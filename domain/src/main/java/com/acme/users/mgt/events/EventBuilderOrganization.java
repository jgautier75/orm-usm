package com.acme.users.mgt.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;
import com.acme.users.mgt.utils.EventChangeUtils;

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
                Optional<AuditChange> hasCodeChanged = EventChangeUtils.compareStrings(META_COMMONS_CODE,
                                previous != null ? previous.getCode() : null,
                                current != null ? current.getCode() : null);
                hasCodeChanged.ifPresent(auditChanges::add);

                // Country
                Optional<AuditChange> hasCountryChanged = EventChangeUtils.compareStrings(META_COMMONS_COUNTRY,
                                previous != null ? previous.getCountry() : null,
                                current != null ? current.getCountry() : null);
                hasCountryChanged.ifPresent(auditChanges::add);

                // Label
                Optional<AuditChange> hasLabelChanged = EventChangeUtils.compareStrings(META_COMMONS_LABEL,
                                previous != null ? previous.getLabel() : null,
                                current != null ? current.getLabel() : null);
                hasLabelChanged.ifPresent(auditChanges::add);

                // Kind
                Optional<AuditChange> hasKindChanged = EventChangeUtils.compareOrganizationKind(META_COMMONS_KIND,
                                previous != null ? previous.getKind() : null,
                                current != null ? current.getKind() : null);
                hasKindChanged.ifPresent(auditChanges::add);

                // Status
                Optional<AuditChange> hasStatusChanged = EventChangeUtils.compareOrganizationStatus(META_COMMONS_STATUS,
                                previous != null ? previous.getStatus() : null,
                                current != null ? current.getStatus() : null);
                hasStatusChanged.ifPresent(auditChanges::add);

                return auditChanges;
        }

}
