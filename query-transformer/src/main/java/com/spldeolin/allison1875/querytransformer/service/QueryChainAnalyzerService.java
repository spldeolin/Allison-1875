package com.spldeolin.allison1875.querytransformer.service;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.service.impl.QueryChainAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(QueryChainAnalyzerServiceImpl.class)
public interface QueryChainAnalyzerService {

    ChainAnalysisDto analyzeQueryChain(MethodCallExpr chain, DesignMetaDto designMeta)
            throws IllegalChainException;

}