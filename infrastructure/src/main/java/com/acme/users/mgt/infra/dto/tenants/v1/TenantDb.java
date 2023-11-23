package com.acme.users.mgt.infra.dto.tenants.v1;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class TenantDb implements IVersioned {
    private Long id;
    private String uid;
    private String code;
    private String label;

    @Override
    public ApiVersion getVersion() {
        return ApiVersion.V1;
    }

}
