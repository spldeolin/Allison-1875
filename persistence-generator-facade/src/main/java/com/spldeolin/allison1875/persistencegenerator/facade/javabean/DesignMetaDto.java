package com.spldeolin.allison1875.persistencegenerator.facade.javabean;

import java.util.LinkedHashMap;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-10-06
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DesignMetaDto {

    String designQualifier;

    String designName;

    String entityQualifier;

    String entityName;

    String mapperQualifier;

    String mapperName;

    List<String> mapperPaths;

    LinkedHashMap<String, PropertyDto> properties;

    String tableName;

    /**
     * 如果有逻辑删除，怎么样算作“数据未被删”
     * 如果properties中有逻辑删除标识，则值来自PersistenceGeneratorConfig#notDeletedSql
     */
    String notDeletedSql;

}