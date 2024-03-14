package com.acme.users.mgt.dto.port.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchFilterDto {
    /**
     * Filter expression.
     * (property)(comparison)(value)(operand)(property)(comparison)(value)
     * E.g: name eq 'Rennes' or name eq 'Cesson'
     * Comparison:
     * <ul>
     * <li>eq: Equals</li>
     * <li>ne: NotEquals</li>
     * <li>gt: Greater than</li>
     * <li>ge: Greater or equals</li>
     * <li>lt: Lower than</li>
     * <li>le: Lower or equals</li>
     * <li>lk: Like</li>
     * </ul>
     * Operand:
     * <ul>
     * <li>and: Logical AND</li>
     * <li>or: Logical OR</li>
     * </ul>
     */
    private String filter;
    /**
     * Nb of results per page.
     */
    private Integer pageSize;
    /**
     * Page index.
     */
    private Integer pageIndex;
    /**
     * Order by expressio: (+-)column.
     * E.g: +name sorts by name in ascending order
     */
    private String orderBy;
}
