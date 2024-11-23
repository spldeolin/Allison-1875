package com.spldeolin.allison1875.querytransformer.javabean;

import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;

/**
 * @author Deolin 2024-11-23
 */
public interface Binary {

    PropertyDto getProperty();

    String getVarName();

    Expression getArgument();

}
