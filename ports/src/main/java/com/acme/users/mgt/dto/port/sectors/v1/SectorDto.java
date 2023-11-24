package com.acme.users.mgt.dto.port.sectors.v1;

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
public class SectorDto implements IVersioned {
    private String code;
    private String label;
    private String parentUid;

    @Override
    public ApiVersion getVersion() {
        return ApiVersion.V1;
    }

}
