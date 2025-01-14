package com.spldeolin.allison1875.startransformer.service;

import java.util.List;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.StarChainServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(StarChainServiceImpl.class)
public interface StarChainService {

    List<MethodCallExpr> detectStarChains(BlockStmt block);

    ChainAnalysisDto analyzeStarChain(MethodCallExpr starChain, AstForest astForest) throws IllegalChainException;

}