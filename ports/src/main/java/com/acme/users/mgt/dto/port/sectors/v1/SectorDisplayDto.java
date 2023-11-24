package com.acme.users.mgt.dto.port.sectors.v1;

import java.util.ArrayList;
import java.util.List;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class SectorDisplayDto implements IVersioned {
    private String uid;
    private String code;
    private String label;
    private boolean root;
    private String parentUid;
    private List<SectorDisplayDto> children;

    @Override
    public ApiVersion getVersion() {
        return ApiVersion.V1;
    }

    public void addSector(SectorDisplayDto sectorDisplayDto) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(sectorDisplayDto);
    }
}
