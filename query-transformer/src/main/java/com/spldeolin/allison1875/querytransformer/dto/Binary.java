package com.spldeolin.allison1875.querytransformer.dto;

import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;

/**
 * @author Deolin 2024-11-23
 */
public interface Binary {

    PropertyDTO getProperty();

    String getVarName();

    Expression getArgument();

}
