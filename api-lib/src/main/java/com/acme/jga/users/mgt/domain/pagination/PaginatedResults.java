package com.acme.jga.users.mgt.domain.pagination;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaginatedResults<T> {
    private Integer nbResults;
    private Integer nbPages;
    private List<T> results;
}
