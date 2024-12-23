package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.dto.InformationSchemaDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.TableStructureAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.JavaTypeNamingDTO;
import com.spldeolin.allison1875.persistencegenerator.service.JdbcTypeService;

/**
 * @author Deolin 2021-03-23
 */
@Singleton
public class JdbcTypeServiceImpl implements JdbcTypeService {

    @Override
    public JavaTypeNamingDTO jdbcType2javaType(InformationSchemaDTO columnMeta,
            TableStructureAnalysisDTO tableStructureAnalysis) {
        String columnType = columnMeta.getColumnType();
        String dataType = columnMeta.getDataType();
        if (columnType == null || dataType == null) {
            throw new IllegalArgumentException("illegal argument.");
        }
        if (StringUtils.containsIgnoreCase(columnType, "tinyint(1)")) {
            return new JavaTypeNamingDTO().setClass(Boolean.class);
        }
        if (StringUtils.equalsAnyIgnoreCase(dataType, "varchar", "char", "text", "longtext")) {
            return new JavaTypeNamingDTO().setClass(String.class);
        }
        if ("tinyint".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(Byte.class);
        }
        if ("int".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(Integer.class);
        }
        if ("bigint".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(Long.class);
        }
        if ("date".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(Date.class);
        }
        if ("time".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(Date.class);
        }
        if ("datetime".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(Date.class);
        }
        if ("timestamp".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(Date.class);
        }
        if ("decimal".equals(dataType)) {
            return new JavaTypeNamingDTO().setClass(BigDecimal.class);
        }
        return null;
    }

}