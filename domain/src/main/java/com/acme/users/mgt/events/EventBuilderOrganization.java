package com.acme.users.mgt.events;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditOperation;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;

@Component
public class EventBuilderOrganization {
        private static final String META_COMMONS_PREFIX = "commons.";
        private static final String META_COMMONS_CODE = META_COMMONS_PREFIX + "code";
        private static final String META_COMMONS_COUNTRY = META_COMMONS_PREFIX + "country";
        private static final String META_COMMONS_LABEL = META_COMMONS_PREFIX + "label";
        private static final String META_COMMONS_KIND = META_COMMONS_PREFIX + "kind";
        private static final String META_COMMONS_STATUS = META_COMMONS_PREFIX + "status";

        public List<AuditChange> buildAuditsChange(OrganizationCommons previous, OrganizationCommons current) {
                List<AuditChange> auditChanges = new ArrayList<>();

                // Code
                if (previous.getCode() == null && current.getCode() != null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_CODE)
                                        .operation(AuditOperation.ADD)
                                        .to(current.getCode())
                                        .build());
                } else if (previous.getCode() != null && current.getCode() != null
                                && !previous.getCode().equals(current.getCode())) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_CODE)
                                        .operation(AuditOperation.UPDATE)
                                        .from(previous.getCode())
                                        .to(current.getCode())
                                        .build());
                } else if (previous.getCode() != null && current.getCode() == null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_CODE)
                                        .operation(AuditOperation.REMOVE)
                                        .from(current.getCode())
                                        .build());
                }

                // Country
                if (previous.getCountry() == null && current.getCountry() != null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_COUNTRY)
                                        .operation(AuditOperation.ADD)
                                        .to(current.getCountry())
                                        .build());
                } else if (previous.getCountry() != null && current.getCountry() != null
                                && !previous.getCountry().equals(current.getCountry())) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_COUNTRY)
                                        .operation(AuditOperation.UPDATE)
                                        .from(previous.getCountry())
                                        .to(current.getCountry())
                                        .build());
                } else if (previous.getCountry() != null && current.getCountry() == null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_COUNTRY)
                                        .operation(AuditOperation.REMOVE)
                                        .from(current.getCountry())
                                        .build());
                }

                // Label
                if (previous.getLabel() == null && current.getLabel() != null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_LABEL)
                                        .operation(AuditOperation.ADD)
                                        .to(current.getLabel())
                                        .build());
                } else if (previous.getLabel() != null && current.getLabel() != null
                                && !previous.getLabel().equals(current.getLabel())) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_LABEL)
                                        .operation(AuditOperation.UPDATE)
                                        .from(previous.getLabel())
                                        .to(current.getLabel())
                                        .build());
                } else if (previous.getLabel() != null && current.getLabel() == null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_LABEL)
                                        .operation(AuditOperation.REMOVE)
                                        .from(previous.getLabel())
                                        .build());
                }

                // Kind
                if (previous.getKind() == null && current.getKind() != null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_KIND)
                                        .operation(AuditOperation.ADD)
                                        .to(current.getKind().getLabel())
                                        .build());
                } else if (previous.getKind() != null && current.getKind() != null
                                && !previous.getKind().equals(current.getKind())) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_KIND)
                                        .operation(AuditOperation.UPDATE)
                                        .from(previous.getKind().getLabel())
                                        .to(current.getKind().getLabel())
                                        .build());
                } else if (previous.getKind() != null && current.getKind() == null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_KIND)
                                        .operation(AuditOperation.REMOVE)
                                        .from(previous.getKind().getLabel())
                                        .build());
                }

                // Status
                if (previous.getStatus() == null && current.getStatus() != null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_STATUS)
                                        .operation(AuditOperation.ADD)
                                        .to(Integer.toString(current.getStatus().getCode()))
                                        .build());
                } else if (previous.getStatus() != null && current.getStatus() != null
                                && !previous.getStatus().equals(current.getStatus())) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_STATUS)
                                        .operation(AuditOperation.UPDATE)
                                        .from(previous.getStatus().getLabel())
                                        .to(current.getStatus().getLabel()).build());
                } else if (previous.getStatus() != null && current.getStatus() == null) {
                        auditChanges.add(AuditChange.builder()
                                        .object(META_COMMONS_STATUS)
                                        .operation(AuditOperation.REMOVE)
                                        .from(previous.getStatus().getLabel())
                                        .build());
                }

                return auditChanges;
        }

}
