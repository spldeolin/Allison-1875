package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-11-12
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateJoinDesignArgs {

    TableStructureAnalysisDto tableStructureAnalysis;

    JavabeanGeneration entityGeneration;

    AstForest astForest;

    CompilationUnit joinDesignCu;

    String designQualifier;

}