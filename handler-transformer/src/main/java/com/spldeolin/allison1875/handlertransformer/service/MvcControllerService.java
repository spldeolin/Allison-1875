package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateServiceAndImplRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDTO;
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