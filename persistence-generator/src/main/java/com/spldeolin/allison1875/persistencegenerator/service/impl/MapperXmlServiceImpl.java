package com.spldeolin.allison1875.persistencegenerator.service.impl;

import static com.spldeolin.allison1875.common.constant.BaseConstant.SINGLE_INDENT;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.dto.KeyMethodNameDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByKeysDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.ReplaceMapperXmlMethodsArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.TableStructureAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.persistencegenerator.service.MapperXmlService;
import com.spldeolin.allison1875.persistencegenerator.util.TextUtils;

/**
 * @author Deolin 2023-12-24
 */
@Singleton
public class MapperXmlServiceImpl implements MapperXmlService {

    private static final String startMark = "[START]";

    private static final String endMark = "[END]";

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private PersistenceGeneratorConfig config;

    /**
     * <sql id="all"></sql> 标签
     */
    @Override
    public List<String> generateAllCloumnSql(TableStructureAnalysisDTO persistence) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add("<sql id=\"all\">");
        xmlLines.addAll(TextUtils.formatLines(BaseConstant.SINGLE_INDENT,
                persistence.getProperties().stream().map(one -> "`" + one.getColumnName() + "`")
                        .collect(Collectors.toList()), 120));
        xmlLines.add("</sql>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateBatchInsertEvenNullMethod(TableStructureAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("INSERT INTO `%s`", persistence.getTableName()));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "( <include refid=\"all\"/> )");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "VALUES");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\",\">(");
        xmlLines.addAll(TextUtils.formatLines(BaseConstant.TREBLE_INDENT,
                persistence.getProperties().stream().map(p -> "#{one." + p.getPropertyName() + "}")
                        .collect(Collectors.toList()), 120));
        xmlLines.add(BaseConstant.DOUBLE_INDENT + ")</foreach>");
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateBatchInsertMethod(TableStructureAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\">", methodName));
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "INSERT INTO `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> `%s`, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<trim prefix=\"VALUE (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> #{one.%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>;");
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        xmlLines.add("</insert>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateBatchUpdateEvenNullMethod(TableStructureAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<update id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "SET");
        for (PropertyDTO nonId : persistence.getNonIdProperties()) {
            xmlLines.add(
                    BaseConstant.TREBLE_INDENT + "`" + nonId.getColumnName() + "` = #{one." + nonId.getPropertyName()
                            + "},");
        }
        // 删除最后一个语句中，最后的逗号
        if (CollectionUtils.isNotEmpty(xmlLines)) {
            int last = xmlLines.size() - 1;
            xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "WHERE 1 = 1");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND " + config.getNotDeletedSql());
        }
        for (PropertyDTO idProperty : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{one."
                    + idProperty.getPropertyName() + "}");
        }
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>");
        xmlLines.add("</update>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateBatchUpdateMethod(TableStructureAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<update id=\"%s\">", methodName));
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<foreach collection=\"entities\" item=\"one\" separator=\";\">");
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<set>");
        for (PropertyDTO nonId : persistence.getNonIdProperties()) {
            xmlLines.add(
                    BaseConstant.TREBLE_INDENT + String.format("<if test=\"one.%s!=null\"> `%s` = #{one.%s}, </if>",
                            nonId.getPropertyName(), nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "</set>");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "WHERE 1 = 1");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND " + config.getNotDeletedSql());
        }
        for (PropertyDTO idProperty : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{one."
                    + idProperty.getPropertyName() + "}");
        }
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</foreach>");
        xmlLines.add("</update>");
        xmlLines.add("");
        return xmlLines;
    }

    /**
     * 根据外键删除
     *
     * 表中每有几个外键，这个Proc就生成几个方法，以_id结尾的字段算作外键
     */
    @Override
    public List<String> generateDeleteByKeyMethod(TableStructureAnalysisDTO persistence,
            List<KeyMethodNameDTO> KeyAndMethodNames) {
        List<String> result = Lists.newArrayList();
        for (KeyMethodNameDTO KeyAndMethodName : KeyAndMethodNames) {
            List<String> xmlLines = Lists.newArrayList();
            PropertyDTO key = KeyAndMethodName.getKey();
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", KeyAndMethodName.getMethodName(),
                    key.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            xmlLines.add(SINGLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
            xmlLines.add(SINGLE_INDENT + "SET " + config.getDeletedSql());
            xmlLines.add(SINGLE_INDENT + "WHERE `" + key.getColumnName() + "` = #{" + key.getPropertyName() + "}");
            xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            xmlLines.add("</update>");
            result.addAll(xmlLines);
            result.add("");
        }
        return result;
    }

    @Override
    public List<String> generateInsertOrUpdateMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\" parameterType=\"%s\">", methodName, entityName));
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "INSERT INTO `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s`, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> #{%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "ON DUPLICATE KEY UPDATE");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim suffixOverrides=\",\">");
        for (PropertyDTO nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s` = #{%s}, </if>",
                    nonId.getPropertyName(), nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        xmlLines.add("</insert>");

        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateInsertMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<insert id=\"%s\" parameterType=\"%s\">", methodName, entityName));
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "INSERT INTO `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s`, </if>",
                    property.getPropertyName(), property.getColumnName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> #{%s}, </if>",
                    property.getPropertyName(), property.getPropertyName()));
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "</trim>");
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        xmlLines.add("</insert>");

        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateListAllMethod(TableStructureAnalysisDTO persistence, String methodName) {
        List<String> result = Lists.newArrayList();
        String firstLine = "<select id=\"" + methodName + "\" ";
        firstLine += "resultMap=\"all\">";
        result.add(firstLine);
        if (config.getEnableGenerateFormatterMarker()) {
            result.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        result.add(BaseConstant.SINGLE_INDENT + "SELECT");
        result.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
        result.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
        result.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
        if (persistence.getIsDeleteFlagExist()) {
            result.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
        }
        if (config.getEnableGenerateFormatterMarker()) {
            result.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        result.add("</select>");
        result.add("");
        return result;
    }

    @Override
    public List<String> generateQueryByEntityMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(
                String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName, entityName));
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
        }
        xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
        xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
        if (persistence.getIsDeleteFlagExist()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
        }
        for (PropertyDTO property : persistence.getProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("  <if test=\"%s!=null\"> AND `%s` = #{%s} </if>",
                    property.getPropertyName(), property.getColumnName(), property.getPropertyName()));
        }
        if (config.getEnableGenerateFormatterMarker()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
        }
        xmlLines.add("</select>");
        xmlLines.add("");
        return xmlLines;
    }

    /**
     * 这个Proc生成2中方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    @Override
    public List<String> generateQueryByIdsMethod(TableStructureAnalysisDTO persistence, String methodName) {
        if (methodName == null) {
            return null;
        }
        List<String> xmlLines = Lists.newArrayList();
        if (persistence.getIdProperties().size() == 1) {
            PropertyDTO onlyPk = Iterables.getOnlyElement(persistence.getIdProperties());
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">", methodName,
                    onlyPk.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + onlyPk.getColumnName() + String.format(
                    "` IN (<foreach collection=\"%s\" item=\"one\" separator=\",\">#{one}</foreach>)",
                    English.plural(MoreStringUtils.toLowerCamel(onlyPk.getPropertyName()))));
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            }
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public List<String> generateQueryByIdMethod(TableStructureAnalysisDTO persistence, String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            String firstLine = "<select id=\"" + methodName + "\" ";
            if (persistence.getIdProperties().size() == 1) {
                firstLine += "parameterType=\"" + Iterables.getOnlyElement(persistence.getIdProperties()).getJavaType()
                        .getQualifier().replaceFirst("java\\.lang\\.", "") + "\" ";
            }
            firstLine += "resultMap=\"all\">";
            xmlLines.add(firstLine);
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
            }
            for (PropertyDTO idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{"
                        + idProperty.getPropertyName() + "}");
            }
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            }
            xmlLines.add("</select>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    /**
     * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
     */
    @Override
    public List<String> generateQueryByKeysMethod(TableStructureAnalysisDTO persistence,
            List<QueryByKeysDTO> queryByKeysDTOs) {
        List<String> sourceCodeLines = Lists.newArrayList();
        for (QueryByKeysDTO queryByKeysDTO : queryByKeysDTOs) {
            List<String> xmlLines = Lists.newArrayList();
            PropertyDTO key = queryByKeysDTO.getKey();
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                    queryByKeysDTO.getMethodName(),
                    key.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + key.getColumnName() + String.format(
                    "` IN (<foreach collection=\"%s\" item=\"one\" separator=\",\">#{one}</foreach>)",
                    queryByKeysDTO.getVarsName()));
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            }
            xmlLines.add("</select>");
            sourceCodeLines.addAll(xmlLines);
            sourceCodeLines.add("");
        }
        return sourceCodeLines;
    }

    @Override
    public List<String> generateQueryByKeyMethod(TableStructureAnalysisDTO persistence,
            List<KeyMethodNameDTO> keyAndMethodNames) {
        List<String> result = Lists.newArrayList();
        for (KeyMethodNameDTO keyAndMethodName : keyAndMethodNames) {
            List<String> xmlLines = Lists.newArrayList();
            PropertyDTO key = keyAndMethodName.getKey();
            xmlLines.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultMap=\"all\">",
                    keyAndMethodName.getMethodName(),
                    key.getJavaType().getQualifier().replaceFirst("java\\.lang\\.", "")));
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
            xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid=\"all\"/>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + key.getColumnName() + "` = #{" + key.getPropertyName()
                    + "}");
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            }
            xmlLines.add("</select>");
            result.addAll(xmlLines);
            result.add("");
        }
        return result;
    }

    @Override
    public List<String> generateResultMap(TableStructureAnalysisDTO persistence, String entityName) {
        List<String> xmlLines = Lists.newArrayList();
        xmlLines.add(String.format("<resultMap id=\"all\" type=\"%s\">", entityName));
        for (PropertyDTO id : persistence.getIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<id column=\"%s\" property=\"%s\"/>",
                    id.getColumnName(), id.getPropertyName()));
        }
        for (PropertyDTO nonId : persistence.getNonIdProperties()) {
            xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("<result column=\"%s\" property=\"%s\"/>",
                    nonId.getColumnName(), nonId.getPropertyName()));
        }
        xmlLines.add("</resultMap>");
        xmlLines.add("");
        return xmlLines;
    }

    @Override
    public List<String> generateUpdateByIdEvenNullMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", methodName, entityName));
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "SET");
            for (PropertyDTO nonId : persistence.getNonIdProperties()) {
                xmlLines.add(
                        BaseConstant.DOUBLE_INDENT + "`" + nonId.getColumnName() + "` = #{" + nonId.getPropertyName()
                                + "},");
            }
            // 删除最后一个语句中，最后的逗号
            if (CollectionUtils.isNotEmpty(xmlLines)) {
                int last = xmlLines.size() - 1;
                xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
            }
            for (PropertyDTO idProperty : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND `" + idProperty.getColumnName() + "` = #{"
                        + idProperty.getPropertyName() + "}");
            }
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            }
            xmlLines.add("</update>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public List<String> generateUpdateByIdMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName) {
        List<String> xmlLines = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(persistence.getIdProperties())) {
            xmlLines.add(String.format("<update id=\"%s\" parameterType=\"%s\">", methodName, entityName));
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "UPDATE `" + persistence.getTableName() + "`");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "<set>");
            for (PropertyDTO nonId : persistence.getNonIdProperties()) {
                xmlLines.add(BaseConstant.DOUBLE_INDENT + String.format("<if test=\"%s!=null\"> `%s` = #{%s}, </if>",
                        nonId.getPropertyName(), nonId.getColumnName(), nonId.getPropertyName()));
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</set>");
            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE 1 = 1");
            if (persistence.getIsDeleteFlagExist()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + "  AND " + config.getNotDeletedSql());
            }
            for (PropertyDTO id : persistence.getIdProperties()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + String.format("  AND `%s` = #{%s}", id.getColumnName(),
                        id.getPropertyName()));
            }
            if (config.getEnableGenerateFormatterMarker()) {
                xmlLines.add(BaseConstant.SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
            }
            xmlLines.add(BaseConstant.SINGLE_INDENT + "</update>");
            xmlLines.add("");
        }
        return xmlLines;
    }

    @Override
    public FileFlush replaceMapperXmlMethods(ReplaceMapperXmlMethodsArgs args) {
        try {
            // find
            File mapperXmlFile = args.getMapperXmlDirectory()
                    .resolve(args.getTableStructureAnalysisDTO().getMapperName() + ".xml").toFile();

            if (!mapperXmlFile.exists()) {
                // create new File
                List<String> sourceCodeLines = Lists.newArrayList();
                sourceCodeLines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                sourceCodeLines.add("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis"
                        + ".org/dtd/mybatis-3-mapper.dtd\">");
                sourceCodeLines.add(String.format("<mapper namespace=\"%s\">", args.getMapper().getFullyQualifiedName()
                        .orElseThrow(() -> new Allison1875Exception(
                                "Node '" + args.getMapper().getName() + "' has no Qualifier"))));
                sourceCodeLines.add("</mapper>");
                FileUtils.writeLines(mapperXmlFile, StandardCharsets.UTF_8.name(), sourceCodeLines);
            }

            List<String> newLines = Lists.newArrayList();

            String content = FileUtils.readFileToString(mapperXmlFile, StandardCharsets.UTF_8);
            List<String> lines = MoreStringUtils.splitLineByLine(content);
            List<String> generatedLines = getGeneratedLines(args.getSourceCodes(), args.getTableStructureAnalysisDTO());

            if (StringUtils.containsAny(content, startMark, endMark)) {
                boolean inAnchorRange = false;
                for (String line : lines) {
                    if (!inAnchorRange) {
                        if (StringUtils.containsAny(line, startMark, endMark)) {
                            // 从 范围外 进入
                            inAnchorRange = true;
                        } else {
                            newLines.add(line);
                        }
                    } else {
                        if (StringUtils.containsAny(line, startMark, endMark)) {
                            // 从 范围内 离开
                            inAnchorRange = false;
                            newLines.addAll(generatedLines);
                        }
                    }
                }
            } else {
                Collections.reverse(lines);
                for (String line : lines) {
                    newLines.add(line);
                    if (line.contains("</mapper>")) {
                        Collections.reverse(generatedLines);
                        newLines.addAll(generatedLines);
                    }
                }
                Collections.reverse(newLines);
            }

            return FileFlush.build(mapperXmlFile, Joiner.on(BaseConstant.NEW_LINE).join(newLines));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String concatXmlComment(TableStructureAnalysisDTO persistence) {
        String result = "<!--";
        if (commonConfig.getEnableNoModifyAnnounce()) {
            result += " " + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (commonConfig.getEnableLotNoAnnounce()) {
            result += " " + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        result += " -->";
        return result;
    }

    private List<String> getGeneratedLines(List<List<String>> sourceCodes, TableStructureAnalysisDTO persistence) {
        List<String> auto = Lists.newArrayList();
        auto.add(BaseConstant.SINGLE_INDENT + concatXmlComment(persistence).replace("<!--", "<!-- " + startMark));
        auto.add("");
        for (List<String> sourceCode : sourceCodes) {
            if (CollectionUtils.isNotEmpty(sourceCode)) {
                for (String line : sourceCode) {
                    if (StringUtils.isNotBlank(line)) {
                        auto.add(BaseConstant.SINGLE_INDENT + line);
                    } else {
                        auto.add("");
                    }
                }
            }
        }
        if (StringUtils.isEmpty(auto.get(auto.size() - 1))) {
            auto.remove(auto.size() - 1);
        }
        auto.add("");
        auto.add(BaseConstant.SINGLE_INDENT + concatXmlComment(persistence).replace("<!--", "<!-- " + endMark));
        return auto;
    }

}
