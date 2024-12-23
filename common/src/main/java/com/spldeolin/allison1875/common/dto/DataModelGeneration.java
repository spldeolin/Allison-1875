package com.spldeolin.allison1875.common.dto;

import java.nio.file.Path;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.common.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-12-30
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataModelGeneration {

    CompilationUnit cu;

    FileFlush fileFlush;

    String dtoName;

    String dtoQualifier;

    ClassOrInterfaceDeclaration coid;

    Path path;

}