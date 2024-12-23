package com.spldeolin.allison1875.persistencegenerator.dto;

import java.nio.file.Path;
import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
public class ReplaceMapperXmlMethodsArgs {

    TableStructureAnalysisDTO tableStructureAnalysisDTO;

    ClassOrInterfaceDeclaration mapper;

    Path mapperXmlDirectory;

    List<List<String>> sourceCodes;

}