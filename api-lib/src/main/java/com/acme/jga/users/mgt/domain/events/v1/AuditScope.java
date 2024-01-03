package com.acme.jga.users.mgt.domain.events.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class AuditScope {
    private String tenantUid;
    private String tenantName;
    private String organizationUid;
    private String organizationName;
}
