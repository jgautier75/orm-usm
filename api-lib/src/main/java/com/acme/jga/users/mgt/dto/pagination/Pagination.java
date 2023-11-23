package com.acme.jga.users.mgt.dto.pagination;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class Pagination {
	private Integer pageSize;
	private Integer page;
	private List<OrderByClause> sorts;
}
