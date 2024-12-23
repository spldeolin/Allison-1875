package com.spldeolin.allison1875.persistencegenerator.dto;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
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
public class DetectOrGenerateMapperRetval {

    /**
     * Allison 1875生成的方法被删除，用户自定义方法被*临时*删除的mapper
     */
    ClassOrInterfaceDeclaration mapper;

    CompilationUnit mapperCu;

    /**
     * 被临时删除的Mapper中所有开发者自定义方法
     */
    final List<MethodDeclaration> customMethods = Lists.newArrayList();

}