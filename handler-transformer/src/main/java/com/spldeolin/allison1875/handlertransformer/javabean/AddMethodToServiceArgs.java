package com.spldeolin.allison1875.handlertransformer.javabean;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.spldeolin.allison1875.common.ast.AstForest;
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

    InitDecAnalysisDto initDecAnalysisDto;

    MethodDeclaration serviceMethod;

    AstForest astForest;

    GenerateServiceAndImplRetval generateServiceAndImplRetval;

}