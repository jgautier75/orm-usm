package com.acme.users.mgt.utils;

import java.util.Optional;

import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditOperation;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventChangeUtils {

    public static Optional<AuditChange> compareStrings(String fieldName, String previous, String current) {
        AuditChange auditChange = null;
        if (previous == null && current != null) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.ADD)
                    .to(current)
                    .build();
        } else if (previous != null && current != null && !previous.equals(current)) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.UPDATE)
                    .from(previous)
                    .to(current)
                    .build();
        } else if (previous != null && current == null) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.REMOVE)
                    .from(previous)
                    .build();
        }
        return Optional.ofNullable(auditChange);
    }

    public static Optional<AuditChange> compareOrganizationKind(String fieldName, OrganizationKind previous,
            OrganizationKind current) {
        AuditChange auditChange = null;
        if (previous == null && current != null) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.ADD)
                    .to(current.getLabel())
                    .build();
        } else if (previous != null && current != null
                && !previous.equals(current)) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.UPDATE)
                    .from(previous.getLabel())
                    .to(current.getLabel())
                    .build();
        } else if (previous != null && current == null) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.REMOVE)
                    .from(previous.getLabel())
                    .build();
        }
        return Optional.ofNullable(auditChange);
    }

    public static Optional<AuditChange> compareOrganizationStatus(String fieldName, OrganizationStatus previous,
            OrganizationStatus current) {
        AuditChange auditChange = null;
        if (previous == null && current != null) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.ADD)
                    .to(Integer.toString(current.getCode()))
                    .build();
        } else if (previous != null && current != null && !previous.equals(current)) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.UPDATE)
                    .from(previous.getLabel())
                    .to(current.getLabel()).build();
        } else if (previous != null && current == null) {
            auditChange = AuditChange.builder()
                    .object(fieldName)
                    .operation(AuditOperation.REMOVE)
                    .from(previous.getLabel())
                    .build();
        }
        return Optional.ofNullable(auditChange);

    }

}
