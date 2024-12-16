package com.spldeolin.allison1875.docanalyzer.javabean;

import java.lang.reflect.Method;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-04
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MvcHandlerDTO {

    MvcControllerDTO mvcController;

    String cat;

    MethodDeclaration md;

    Method reflection;

}