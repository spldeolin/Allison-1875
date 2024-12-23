package com.spldeolin.allison1875.common.dto;

import com.github.javaparser.ast.body.FieldDeclaration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-14
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddInjectFieldRetval {

    FieldDeclaration field;

    String fieldVarName;

}