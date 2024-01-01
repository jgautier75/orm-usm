package com.acme.users.mgt.infra.dto.events.v1;

import java.time.LocalDateTime;

import com.acme.jga.users.mgt.domain.events.v1.AuditAction;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.jga.users.mgt.domain.events.v1.EventTarget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class AuditEventDb {
    private String uid;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private EventTarget target;
    private String objectUid;
    private AuditAction action;
    private EventStatus status;
    private String payload;
}
