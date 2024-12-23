package com.spldeolin.allison1875.querytransformer.dto;

import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.querytransformer.enums.JoinTypeEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-11-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinClauseDTO {

    JoinTypeEnum joinType;

    @JsonSerialize(using = ToStringSerializer.class)
    DesignMetaDTO joinedDesignMeta;

    Set<JoinedPropertyDTO> joinedProperties;

    Set<JoinConditionDTO> joinConditions;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JoinClauseDTO that = (JoinClauseDTO) o;
        return joinType == that.joinType && Objects.equals(joinedDesignMeta, that.joinedDesignMeta) && Objects.equals(
                joinedProperties, that.joinedProperties) && Objects.equals(joinConditions, that.joinConditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(joinType, joinedDesignMeta, joinedProperties, joinConditions);
    }

}