package com.spldeolin.allison1875.persistencegenerator.facade.javabean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-07-12
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PropertyDto {

    String columnName;

    String propertyName;

    JavaTypeNamingDto javaType;

    String description;

    Long length;

    Boolean notnull;

    String defaultV;

}