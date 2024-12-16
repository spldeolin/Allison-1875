package com.spldeolin.allison1875.handlertransformer.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.impl.InitDecAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(InitDecAnalyzerServiceImpl.class)
public interface InitDecAnalyzerService {

    InitDecAnalysisDTO analyzeInitDec(CompilationUnit mvcControllerCu, ClassOrInterfaceDeclaration mvcController,
            InitializerDeclaration initDec);

}