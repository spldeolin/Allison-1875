package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
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
public class GenerateDesignArgs {

    TableStructureAnalysisDto tableStructureAnalysis;

    JavabeanGeneration entityGeneration;

    ClassOrInterfaceDeclaration mapper;

    AstForest astForest;

}