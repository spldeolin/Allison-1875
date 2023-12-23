package com.spldeolin.allison1875.base.factory.javabean;

import java.util.List;
import java.util.function.BiConsumer;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.ast.AstForest;
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
public class JavabeanArg {

    @NotNull AstForest astForest;

    @NotBlank String packageName;

    @NotBlank String className;

    String description;

    @NotBlank String authorName = "Allison 1875";

    @NotNull List<@NotNull @Valid FieldArg> fieldArgs = Lists.newArrayList();

    BiConsumer<CompilationUnit, ClassOrInterfaceDeclaration> more4Javabean;

    LotNo lotNo;

}