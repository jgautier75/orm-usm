package com.acme.users.mgt.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditOperation;
import com.acme.jga.users.mgt.domain.sectors.v1.Sector;

@Component
public class EventBuilderSector {

    public List<AuditChange> buildAuditsChange(Sector previous, Sector current) {
        final List<AuditChange> auditChanges = new ArrayList<>();

                DiffResult<Sector> diffResult = previous.diff(current);
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
                                        auditChange = new AuditChange(diff.getFieldName(), operation, null,(String) diff.getRight());
                                        break;
                                case UPDATE:
                                        auditChange = new AuditChange(diff.getFieldName(), operation, (String) diff.getLeft(),(String) diff.getRight());
                                        break;
                                case REMOVE:                                      
                                        auditChange = new AuditChange(diff.getFieldName(), operation, (String) diff.getLeft(),null);
                                        break;
                                default:
                                        break;
                        }
                        auditChanges.add(auditChange);
                }
                
                return auditChanges;
    }

}
