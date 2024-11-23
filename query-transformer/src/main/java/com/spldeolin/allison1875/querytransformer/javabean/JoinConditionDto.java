package com.spldeolin.allison1875.querytransformer.javabean;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
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
public class JoinConditionDto {

    String subjectPropertyName;

    String varName;

    PredicateEnum predicate;

    @JsonSerialize(using = ToStringSerializer.class)
    Expression objectExpr;

    String propertyName4Comparing;

}
