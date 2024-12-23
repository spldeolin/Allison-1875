package com.spldeolin.allison1875.persistencegenerator.dto;

import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
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
public class GenerateJoinChainArgs {

    TableStructureAnalysisDTO tableStructureAnalysis;

    DataModelGeneration entityGeneration;

    CompilationUnit joinChainCu;

    String designQualifier;

}