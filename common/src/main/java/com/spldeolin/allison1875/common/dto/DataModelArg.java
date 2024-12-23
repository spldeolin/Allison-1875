package com.spldeolin.allison1875.common.dto;

import java.util.List;
import java.util.function.BiConsumer;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-05-26
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataModelArg {

    @NotNull AstForest astForest;

    @NotBlank String packageName;

    @NotBlank String className;

    String description;

    @NotBlank String author;

    @Valid
    final List<FieldArg> fieldArgs = Lists.newArrayList();

    BiConsumer<CompilationUnit, ClassOrInterfaceDeclaration> moreOperation;

    @NotNull
    FileExistenceResolutionEnum dataModelExistenceResolution;

    /**
     * 生成的DataModel是否实现java.io.Serializable接口
     */
    @NotNull
    Boolean isDataModelSerializable;

    /**
     * 生成的DataModel是否实现java.lang.Cloneable接口
     */
    @NotNull
    Boolean isDataModelCloneable;

}