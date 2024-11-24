package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.CommentService;
import com.spldeolin.allison1875.persistencegenerator.service.JdbcTypeService;
import com.spldeolin.allison1875.persistencegenerator.service.TableStructureAnalyzerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-12
 */
@Singleton
@Slf4j
public class TableStructureAnalyzerServiceImpl implements TableStructureAnalyzerService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private JdbcTypeService jdbcTypeService;

    @Inject
    private CommentService commentService;

    @Override
    public List<TableStructureAnalysisDto> analyzeTableStructure() {
        // 查询information_schema.COLUMNS、information_schema.TABLES表
        List<InformationSchemaDto> infoSchemas = this.queryInformationSchema();
        String deleteFlag = getDeleteFlagName();

        Map<String, TableStructureAnalysisDto> persistences = Maps.newHashMap();
        for (InformationSchemaDto infoSchema : infoSchemas) {
            TableStructureAnalysisDto dto = new TableStructureAnalysisDto();
            String domainName = MoreStringUtils.toUpperCamel(infoSchema.getTableName());
            dto.setTableName(infoSchema.getTableName());
            dto.setEntityName(domainName + endWith());
            dto.setMapperName(domainName + "Mapper");
            dto.setDescrption(commentService.analyzeTableComment(infoSchema));
            dto.setIdProperties(Lists.newArrayList());
            dto.setNonIdProperties(Lists.newArrayList());
            dto.setKeyProperties(Lists.newArrayList());
            dto.setProperties(Lists.newArrayList());
            persistences.put(infoSchema.getTableName(), dto);
        }
        for (InformationSchemaDto infoSchema : infoSchemas) {
            TableStructureAnalysisDto tableStructureAnalysis = persistences.get(infoSchema.getTableName());
            String columnName = infoSchema.getColumnName();
            PropertyDto property = new PropertyDto();
            property.setColumnName(columnName);
            property.setPropertyName(MoreStringUtils.toLowerCamel(columnName));
            JavaTypeNamingDto javaType = jdbcTypeService.jdbcType2javaType(infoSchema, tableStructureAnalysis);
            if (javaType == null) {
                log.warn("出现了预想外的类型 columnName={} dataType={} columnType={}", infoSchema.getColumnName(),
                        infoSchema.getDataType(), infoSchema.getColumnType());
                continue;
            }
            property.setJavaType(javaType);
            property.setDescription(commentService.analyzeColumnComment(infoSchema));
            property.setLength(infoSchema.getCharacterMaximumLength());
            property.setNotnull("NO".equals(infoSchema.getIsNullable()));
            property.setDefaultV(infoSchema.getColumnDefault());

            tableStructureAnalysis.getProperties().add(property);
            if ("PRI".equalsIgnoreCase(infoSchema.getColumnKey())) {
                tableStructureAnalysis.getIdProperties().add(property);
            } else {
                tableStructureAnalysis.getNonIdProperties().add(property);
                if (columnName.endsWith("_id")) {
                    tableStructureAnalysis.getKeyProperties().add(property);
                }
            }

            if (columnName.equals(deleteFlag)) {
                tableStructureAnalysis.setIsDeleteFlagExist(true);
            }

            String hash = StringUtils.upperCase(HashingUtils.hashString(tableStructureAnalysis.toString()));
            tableStructureAnalysis.setLotNo(String.format("PG%s-%s", Allison1875.SHORT_VERSION, hash));
        }

        reportWhileNoDeleleFlag(deleteFlag, persistences);

        return Lists.newArrayList(persistences.values());
    }

    protected List<InformationSchemaDto> queryInformationSchema() {
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUserName(),
                config.getPassword())) {
            String sql = Resources.toString(Resources.getResource("information_schema.sql"), StandardCharsets.UTF_8);
            String part = "IS NOT NULL";
            List<String> tables = config.getTables();
            if (CollectionUtils.isNotEmpty(tables)) {
                tables = tables.stream().map(one -> "'" + one + "'").collect(Collectors.toList());
                part = Joiner.on(',').appendTo(new StringBuilder("IN ("), tables).append(")").toString();
            }
            sql = sql.replace("${tableNames}", part);
            sql = sql.replace("${tableSchema}", "'" + config.getSchema() + "'");

            System.setProperty("org.jooq.no-logo", "true");
            Result<Record> records = DSL.using(conn, SQLDialect.MYSQL).fetch(sql);
            return records.into(InformationSchemaDto.class);
        } catch (Exception e) {
            log.error("QueryInformationSchemaProc.process", e);
            return Lists.newArrayList();
        }
    }

    private void reportWhileNoDeleleFlag(String deleteFlag, Map<String, TableStructureAnalysisDto> persistences) {
        persistences.values().stream().filter(dto -> !dto.getIsDeleteFlagExist())
                .forEach(dto -> log.info("数据表[{}]没有逻辑删除标识符[{}]", dto.getTableName(), deleteFlag));
    }

    private String getDeleteFlagName() {
        String sql = config.getNotDeletedSql();
        if (StringUtils.isEmpty(sql) || StringUtils.isEmpty(config.getDeletedSql())) {
            return null;
        }
        if (!sql.contains("=")) {
            log.warn("notDeletedSql非法，不是等式，认为没有删除标识符 [{}]", sql);
            return null;
        }
        return sql.split("=")[0].trim();
    }

    private String endWith() {
        return config.getIsEntityEndWithEntity() ? "Entity" : "";
    }

}