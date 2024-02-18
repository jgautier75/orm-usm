package com.acme.users.mgt.events;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.users.mgt.config.KafkaConfig;
import com.acme.users.mgt.events.protobuf.Event.AuditAuthor;
import com.acme.users.mgt.events.protobuf.Event.AuditEventMessage;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventBusHandler implements MessageHandler, InitializingBean {
    private final KafkaConfig kafkaConfig;
    private final KafkaTemplate<String, AuditEventMessage> kakaTemplateAudit;
    private final IEventsInfraService eventsInfraService;
    private final ILogService logService;
    private final PublishSubscribeChannel eventAuditChannel;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    @Override
    public void afterPropertiesSet() throws Exception {
        eventAuditChannel.subscribe(this);
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String callerName = this.getClass().getName();
        logService.debugS(callerName, "Handling wakeup message", null);
        if (!isRunning.get()) {
            isRunning.set(true);
            try {
                List<AuditEvent> auditEvents = eventsInfraService.findPendingEvents();
                if (CollectionUtils.isEmpty(auditEvents)) {
                    logService.warnS(callerName, "No pending event to send", null);
                } else {
                    for (AuditEvent auditEvent : auditEvents) {
                        AuditEventMessage auditEventMessage = protobufConversion(auditEvent);
                        String msgKey = auditEvent.getObjectUid() + "-" + auditEvent.getUid();
                        ProducerRecord<String, AuditEventMessage> producerRecord = new ProducerRecord<>(
                                kafkaConfig.getTopicNameAuditEvents(), msgKey, auditEventMessage);
                        kakaTemplateAudit.send(producerRecord);
                    }
                    List<String> uids = auditEvents.stream().map(AuditEvent::getUid).distinct()
                            .collect(Collectors.toList());
                    eventsInfraService.updateEventsStatus(uids, EventStatus.PROCESSED);
                }
            } finally {
                isRunning.set(false);
            }
        }
    }

    /**
     * Convert audit event to protobuf format.
     * 
     * @param auditEvent Audit event
     * @return Audit event in protobuf format
     */
    private AuditEventMessage protobufConversion(AuditEvent auditEvent) {
        AuditEventMessage.Builder auditEventMessageBuilder = AuditEventMessage.newBuilder();
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        auditEventMessageBuilder.setAction(auditEvent.getAction().name());
        auditEventMessageBuilder.setCreatedAt(auditEvent.getCreatedAt().atZone(ZoneOffset.UTC).format(isoFormatter));
        auditEventMessageBuilder
                .setLastUpdatedAt(auditEvent.getLastUpdatedAt().atZone(ZoneOffset.UTC).format(isoFormatter));
        auditEventMessageBuilder.setUid(auditEvent.getObjectUid());
        if (auditEvent.getTarget() != null) {
            auditEventMessageBuilder.setTarget(auditEvent.getTarget().getValue());
        }
        if (auditEvent.getAuthor() != null) {
            auditEventMessageBuilder.setAuthor(AuditAuthor.newBuilder().setName(auditEvent.getAuthor().getName())
                    .setUid(auditEvent.getAuthor().getUid()).build());
        }
        if (auditEvent.getScope() != null) {
            com.acme.users.mgt.events.protobuf.Event.AuditScope.Builder auditScopeBuilder = com.acme.users.mgt.events.protobuf.Event.AuditScope
                    .newBuilder()
                    .setTenantUid(auditEvent.getScope().getTenantUid())
                    .setTenantName(auditEvent.getScope().getTenantName());
            if (auditEvent.getScope().getOrganizationName() != null) {
                auditScopeBuilder.setOrganizationName(auditEvent.getScope().getOrganizationName());
            }
            if (auditEvent.getScope().getOrganizationUid() != null) {
                auditScopeBuilder.setOrganizationUid(auditEvent.getScope().getOrganizationUid());
            }
            auditEventMessageBuilder.setScope(auditScopeBuilder.build());
        }
        auditEventMessageBuilder.setObjectUid(auditEvent.getObjectUid());
        auditEventMessageBuilder.setAction(auditEvent.getAction().name());
        auditEventMessageBuilder.setStatus(auditEvent.getStatus().getValue());
        if (!CollectionUtils.isEmpty(auditEvent.getChanges())) {
            for (AuditChange auditChange : auditEvent.getChanges()) {
                auditEventMessageBuilder.addChanges(
                        com.acme.users.mgt.events.protobuf.Event.AuditChange.newBuilder().setFrom(auditChange.getFrom())
                                .setObject(auditChange.getObject()).setTo(auditChange.getTo())
                                .setOperation(auditChange.getOperation().name()).build());
            }
        }
        return auditEventMessageBuilder.build();
    }

}
