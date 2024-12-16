package com.spldeolin.allison1875.querytransformer.javabean;

import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDTO;

/**
 * @author Deolin 2024-11-23
 */
public interface Binary {

    PropertyDTO getProperty();

    String getVarName();

    Expression getArgument();

}
