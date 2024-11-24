package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateDtoJavabeansRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDto;
import com.spldeolin.allison1875.handlertransformer.service.impl.ReqRespServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ReqRespServiceImpl.class)
public interface ReqRespService {

    void validInitBody(BlockStmt initBody, InitDecAnalysisDto initDecAnalysis);

    GenerateDtoJavabeansRetval generateDtoJavabeans(InitDecAnalysisDto initDecAnalysis,
            List<ClassOrInterfaceDeclaration> dtos);

}