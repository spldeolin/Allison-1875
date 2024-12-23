package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateDTOsRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.impl.ReqRespServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(ReqRespServiceImpl.class)
public interface ReqRespService {

    void validInitBody(BlockStmt initBody, InitDecAnalysisDTO initDecAnalysis);

    GenerateDTOsRetval generateDTOs(InitDecAnalysisDTO initDecAnalysis, List<ClassOrInterfaceDeclaration> dtos);

}