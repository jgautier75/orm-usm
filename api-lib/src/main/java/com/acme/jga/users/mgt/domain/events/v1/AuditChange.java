package com.acme.jga.users.mgt.domain.events.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class AuditChange {
    private String object;
    private AuditOperation operation;
    private String from;
    private String to;
}
