package com.acme.jga.users.mgt.domain.events.v1;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class AuditEvent {
    private LocalDateTime timestamp;
    private String uid;
    private EventTarget target;
    private AuditAuthor author;
    private AuditScope scope;
    private String objectUid;
    private AuditAction action;
    private EventStatus status;
    private List<AuditChange> changes;
}
