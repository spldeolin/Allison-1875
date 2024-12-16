package com.spldeolin.allison1875.handlertransformer.javabean;

import com.github.javaparser.ast.CompilationUnit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-03-05
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateServiceAndImplArgs {

    CompilationUnit controllerCu;

    InitDecAnalysisDTO initDecAnalysisDTO;

}