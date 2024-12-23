package com.spldeolin.allison1875.common.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-17
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateMvcHandlerArgs {

    @NotEmpty String mvcHandlerUrl;

    String description;

    String reqBodyDTOType;

    String respBodyDTOType;

    @NotEmpty String injectedServiceVarName;

    @NotEmpty String serviceMethodName;

    ClassOrInterfaceDeclaration mvcController;

    Boolean isHttpGet;

    List<VariableDeclarator> reqParams;

}