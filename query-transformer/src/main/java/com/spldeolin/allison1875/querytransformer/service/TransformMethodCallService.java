package com.spldeolin.allison1875.querytransformer.service;

import java.util.List;
import com.github.javaparser.ast.stmt.Statement;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDTO;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.service.impl.TransformMethodCallServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(TransformMethodCallServiceImpl.class)
public interface TransformMethodCallService {

    String methodCallExpr(String mapperVarName, ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration);

    List<Statement> argumentBuildStmts(ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration);

    List<Statement> mapOrMultimapBuildStmts(DesignMetaDTO designMeta, ChainAnalysisDTO chainAnalysis,
            GenerateReturnTypeRetval resultGeneration);

}