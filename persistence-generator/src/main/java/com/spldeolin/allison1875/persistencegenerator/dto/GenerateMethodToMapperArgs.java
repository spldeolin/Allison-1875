package com.spldeolin.allison1875.persistencegenerator.dto;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
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

    TableStructureAnalysisDTO tableStructureAnalysisDTO;

    DataModelGeneration entityGeneration;

    PropertyDTO key;

    ClassOrInterfaceDeclaration mapper;

}
