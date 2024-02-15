package com.acme.users.mgt.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditOperation;
import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.users.mgt.utils.EventChangeUtils;

@Component
public class EventBuilderSector {
    private static final String META_CODE = "code";
    private static final String META_LABEL = "label";
    private static final String META_PARENT = "parent";

    public List<AuditChange> buildAuditsChange(Sector previous, Sector current) {
        List<AuditChange> auditChanges = new ArrayList<>();

        // Code
        Optional<AuditChange> hasCodeChanged = EventChangeUtils.compareStrings(META_CODE, previous.getCode(),
                current.getCode());
        hasCodeChanged.ifPresent(auditChanges::add);

        // Label
        Optional<AuditChange> hasLabelChanged = EventChangeUtils.compareStrings(META_LABEL, previous.getLabel(),
                current.getLabel());
        hasLabelChanged.ifPresent(auditChanges::add);

        // Parent sector - Parent sector is mandatory so it's an update
        if ((previous.getParentId() != null || previous.getParentUid() != null)
                && (current.getParentId() != null || current.getParentUid() != null)) {

            String targetFrom = null;
            String targetTo = null;

            if (previous.getParentUid() != null && current.getParentUid() != null
                    && !previous.getParentUid().equals(current.getParentUid())) {
                targetFrom = previous.getParentUid();
                targetTo = previous.getParentUid();
            } else if (previous.getParentId() != null && current.getParentId() != null
                    && !previous.getParentId().equals(current.getParentId())) {
                targetFrom = previous.getParentId().toString();
                targetTo = current.getParentId().toString();
            }
            auditChanges.add(AuditChange.builder()
                    .object(META_PARENT)
                    .from(targetFrom)
                    .to(targetTo)
                    .operation(AuditOperation.UPDATE)
                    .build());
        }

        return auditChanges;
    }

}
