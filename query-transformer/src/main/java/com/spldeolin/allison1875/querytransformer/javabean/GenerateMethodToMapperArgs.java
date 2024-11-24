package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-13
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateMethodToMapperArgs {

    String mapperQualifier;

    ChainAnalysisDto chainAnalysis;

    List<Parameter> cloneParameters;

    Type clonedReturnType;

    Map<String, ClassOrInterfaceDeclaration> methodAddedMappers;

}