package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.handlertransformer.dto.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.dto.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.impl.MvcControllerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MvcControllerServiceImpl.class)
public interface MvcControllerService {

    List<ClassOrInterfaceDeclaration> detectMvcControllers(CompilationUnit cu);

    void replaceMvcHandlerToInitDec(InitDecAnalysisDTO initDecAnalysisDTO,
            GenerateServiceAndImplRetval generateServiceAndImplRetval,
            GenerateMvcHandlerRetval generateMvcHandlerRetval);

}