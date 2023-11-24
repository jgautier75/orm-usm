package com.acme.jga.users.mgt.domain.sectors.v1;

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
public class Sector {
    private Long id;
    private String uid;
    private String code;
    private String label;
    private Long orgId;
    private boolean root;
    private Long parentId;
    private String parentUid;
    private Long tenantId;
    private List<Sector> children;

    public void addChild(Sector sector) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(sector);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
