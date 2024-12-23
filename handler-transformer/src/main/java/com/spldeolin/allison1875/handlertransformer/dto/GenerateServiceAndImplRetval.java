package com.spldeolin.allison1875.handlertransformer.dto;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-03-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateServiceAndImplRetval {

    ClassOrInterfaceDeclaration service;

    CompilationUnit serviceCu;

    ClassOrInterfaceDeclaration serviceImpl;

    CompilationUnit serviceImplCu;

    String serviceVarName;

    String serviceQualifier;

}