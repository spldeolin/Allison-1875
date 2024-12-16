package com.spldeolin.allison1875.persistencegenerator.javabean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-07-11
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InformationSchemaDTO {

    String tableName;

    String tableComment;

    String columnName;

    String dataType;

    String columnType;

    String columnComment;

    String columnKey;

    Long characterMaximumLength;

    String isNullable; // YES NO

    String columnDefault;

}