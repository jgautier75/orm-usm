package com.acme.users.mgt.events;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.users.mgt.config.KafkaConfig;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventBusHandler implements MessageHandler, InitializingBean {
    private final KafkaConfig kafkaConfig;
    private final KafkaTemplate<String, String> kakaTemplateAudit;
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
            try {
                List<AuditEvent> auditEvents = eventsInfraService.findPendingEvents();
                if (CollectionUtils.isEmpty(auditEvents)) {
                    logService.warnS(callerName, "No pending event to send", null);
                } else {
                    for (AuditEvent auditEvent : auditEvents) {
                        kakaTemplateAudit.send(kafkaConfig.getTopicNameAuditEvents(), auditEvent.getPayload());
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

}
