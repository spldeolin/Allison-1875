package com.spldeolin.allison1875.handlertransformer.dto;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-03-05
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddMethodToServiceArgs {

    CompilationUnit controllerCu;

    InitDecAnalysisDTO initDecAnalysisDTO;

    MethodDeclaration serviceMethod;

    GenerateServiceAndImplRetval generateServiceAndImplRetval;

}