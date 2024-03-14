package com.acme.users.mgt.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditOperation;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;

@Component
public class EventBuilderOrganization {
        private static final String META_COMMONS_PREFIX = "commons.";

        public List<AuditChange> buildAuditsChange(OrganizationCommons previous, OrganizationCommons current) {
                final List<AuditChange> auditChanges = new ArrayList<>();

                DiffResult<OrganizationCommons> diffResult = previous.diff(current);
                AuditChange auditChange = null;

                for (Diff<?> diff : diffResult){
                        AuditOperation operation = AuditOperation.UPDATE;
                        if (diff.getLeft()!=null && diff.getRight()==null){
                                operation = AuditOperation.REMOVE;
                        }else if (diff.getLeft()==null && diff.getRight()!=null){
                                operation = AuditOperation.ADD;
                        }
                        switch (operation) {
                                case ADD:
                                        auditChange = new AuditChange(META_COMMONS_PREFIX+diff.getFieldName(), operation, null,(String) diff.getRight());
                                        break;
                                case UPDATE:
                                        auditChange = new AuditChange(META_COMMONS_PREFIX+diff.getFieldName(), operation, (String) diff.getLeft(),(String) diff.getRight());
                                        break;
                                case REMOVE:                                      
                                        auditChange = new AuditChange(META_COMMONS_PREFIX+diff.getFieldName(), operation, (String) diff.getLeft(),null);
                                        break;
                                default:
                                        break;
                        }
                        auditChanges.add(auditChange);
                }
                
                return auditChanges;
        }

}
