package com.acme.jga.search.filtering.utils;

import java.util.List;

import org.antlr.v4.runtime.tree.ErrorNode;

import com.acme.jga.search.filtering.expr.Expression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParsingResult {
    private List<Expression> expressions;
    private List<ErrorNode> errorNodes;
    private boolean empty;
}
