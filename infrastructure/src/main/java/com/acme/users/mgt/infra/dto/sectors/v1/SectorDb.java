package com.acme.users.mgt.infra.dto.sectors.v1;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class SectorDb {
    private Long tenantId;
    private Long orgId;
    private Long id;
    private String uid;
    private String code;
    private String label;
    private boolean root;
    private Long parentId;
    private List<SectorDb> children;

    public void addChild(SectorDb sectorDb) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(sectorDb);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
