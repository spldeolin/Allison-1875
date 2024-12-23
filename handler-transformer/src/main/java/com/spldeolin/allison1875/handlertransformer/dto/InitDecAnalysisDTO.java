package com.spldeolin.allison1875.handlertransformer.dto;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-22
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitDecAnalysisDTO {

    InitializerDeclaration initDec;

    String mvcHandlerUrl;

    String mvcHandlerMethodName;

    String mvcHandlerDescription;

    CompilationUnit mvcControllerCu;

    ClassOrInterfaceDeclaration mvcController;

    String lotNo;

    @Override
    public String toString() {
        return mvcHandlerUrl + " " + mvcHandlerDescription;
    }

}