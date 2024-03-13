package com.acme.users.mgt.dto.port.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchFilterDto {
    private String filter;
    private Integer pageSize;
    private Integer pageIndex;
}
