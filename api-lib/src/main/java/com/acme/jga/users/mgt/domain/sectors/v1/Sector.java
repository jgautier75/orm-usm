package com.acme.jga.users.mgt.domain.sectors.v1;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class Sector implements Diffable<Sector>{
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

    @Override
    public DiffResult<Sector> diff(Sector obj) {
        return new DiffBuilder<Sector>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
       .append("code", this.code, obj.code)
       .append("label", this.label, obj.label)       
       .append("parentUid", this.parentUid, obj.parentUid)
       .build();
    }
}
