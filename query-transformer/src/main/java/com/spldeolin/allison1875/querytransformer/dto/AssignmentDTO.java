package com.spldeolin.allison1875.querytransformer.dto;

import java.util.Objects;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.javaparser.ast.expr.Expression;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-11-23
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentDTO implements Binary {

    PropertyDTO property;

    String varName;

    @JsonSerialize(using = ToStringSerializer.class)
    Expression argument;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssignmentDTO that = (AssignmentDTO) o;
        return Objects.equals(property, that.property) && Objects.equals(varName, that.varName) && Objects.equals(
                argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, varName, argument);
    }

}