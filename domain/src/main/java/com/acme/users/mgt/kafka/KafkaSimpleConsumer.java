package com.acme.users.mgt.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.acme.users.mgt.events.protobuf.Event.AuditEventMessage;
import com.acme.users.mgt.logging.services.api.ILogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaSimpleConsumer {
    private final ILogService logService;

    @KafkaListener(topics = "${app.kafka.producer.topicNameAuditEvents}", groupId = "${app.kafka.consumer.auditEventsGroupId}")
    public void consume(ConsumerRecord<String, AuditEventMessage> messageRecord) {
        logService.infoS(this.getClass().getName(), "Received message: [%s]", new Object[] { messageRecord.value() });
    }

}
