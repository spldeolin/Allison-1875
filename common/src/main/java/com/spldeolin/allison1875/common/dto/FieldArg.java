package com.spldeolin.allison1875.common.dto;

import java.util.function.BiConsumer;
import javax.validation.constraints.NotBlank;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
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
public class FieldArg {

    String description;

    @NotBlank String typeQualifier;

    @NotBlank String fieldName;

    BiConsumer<ClassOrInterfaceDeclaration, FieldDeclaration> moreOperation;

}