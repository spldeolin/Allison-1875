package com.spldeolin.allison1875.querytransformer.javabean;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDTO;
import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-11-18
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinConditionDTO implements CompareableBinary {

    PropertyDTO property;

    String varName;

    ComparisonOperatorEnum comparisonOperator;

    @JsonSerialize(using = ToStringSerializer.class)
    Expression argument;

    PropertyDTO comparedProperty;

}
