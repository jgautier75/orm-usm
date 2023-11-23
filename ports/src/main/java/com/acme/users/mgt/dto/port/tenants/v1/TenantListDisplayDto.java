package com.acme.users.mgt.dto.port.tenants.v1;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TenantListDisplayDto {
    private List<TenantDisplayDto> tenants;
}
