package com.acme.jga.search.filtering.parser;

import java.util.Collections;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.util.ObjectUtils;

import com.acme.jga.search.filtering.FilterLexer;
import com.acme.jga.search.filtering.FilterParser;
import com.acme.jga.search.filtering.listener.SearchFilterListener;
import com.acme.jga.search.filtering.utils.ParsingResult;

public class QueryParser {
    
    public ParsingResult parseQuery(String query){
        if (ObjectUtils.isEmpty(query)){
            return new ParsingResult(Collections.emptyList(),Collections.emptyList(),true);
        }
        CodePointCharStream stream = CharStreams.fromString(query);
        FilterLexer lexer = new FilterLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FilterParser parser = new FilterParser(tokens);
        SearchFilterListener searchFilterListener = new SearchFilterListener();
        new ParseTreeWalker().walk(searchFilterListener, parser.filter());
        return new ParsingResult(searchFilterListener.getExpressions(), searchFilterListener.getErrors(),false);
    }

}
