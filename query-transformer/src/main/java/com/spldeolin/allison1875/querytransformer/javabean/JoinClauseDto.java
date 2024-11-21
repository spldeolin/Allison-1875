package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.List;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
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
public class JoinClauseDto {

    JoinTypeEnum joinType;

    DesignMetaDto joinedDesignMeta;

    List<JoinedPropertyDto> joinedProperties;

    List<JoinConditionDto> joinConditions;

}