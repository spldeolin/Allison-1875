package com.spldeolin.allison1875.querytransformer.service.impl;

import static com.spldeolin.allison1875.common.constant.BaseConstant.DOUBLE_INDENT;
import static com.spldeolin.allison1875.common.constant.BaseConstant.SINGLE_INDENT;
import static com.spldeolin.allison1875.common.constant.BaseConstant.TREBLE_INDENT;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.KeywordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDTO;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.OrderSequenceEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnShapeEnum;
import com.spldeolin.allison1875.querytransformer.javabean.AssignmentDTO;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateMethodToMapperXmlArgs;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.javabean.JoinClauseDTO;
import com.spldeolin.allison1875.querytransformer.javabean.JoinConditionDTO;
import com.spldeolin.allison1875.querytransformer.javabean.JoinedPropertyDTO;
import com.spldeolin.allison1875.querytransformer.javabean.SearchConditionDTO;
import com.spldeolin.allison1875.querytransformer.javabean.SortPropertyDTO;
import com.spldeolin.allison1875.querytransformer.javabean.XmlSourceFile;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Slf4j
public class MapperLayerServiceImpl implements MapperLayerService {

    public static final String SINGLE_INDENT_WITH_AND = SINGLE_INDENT + "  AND ";

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private QueryTransformerConfig queryTransformerConfig;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public Optional<FileFlush> generateMethodToMapper(GenerateMethodToMapperArgs args) {
        ClassOrInterfaceDeclaration mapper = this.findMapper(args.getMapperQualifier(),
                args.getMethodAddedMappers());
        if (mapper == null) {
            return Optional.empty();
        }

        ChainAnalysisDTO chainAnalysis = args.getChainAnalysis();
        String methodName = chainAnalysis.getMethodName();
        methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, mapper);
        log.info(
                "anti duplication worked completed, new method name '{}' update to ChainAnalysisDTO.methodName, old={}",
                methodName, chainAnalysis.getMethodName());
        chainAnalysis.setMethodName(methodName);

        MethodDeclaration method = new MethodDeclaration();
        if (commonConfig.getEnableLotNoAnnounce()) {
            method.setJavadocComment(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo());
        }
        method.setType(args.getClonedReturnType());
        method.setName(methodName);
        method.setParameters(new NodeList<>(args.getCloneParameters()));
        method.setBody(null);
        mapper.getMembers().add(method);

        CompilationUnit cu = mapper.findCompilationUnit().orElseThrow(() -> new CuAbsentException(mapper));
        importExprService.extractQualifiedTypeToImport(cu);
        return Optional.of(FileFlush.build(cu));
    }

    @Override
    public void generateMethodToMapperXml(GenerateMethodToMapperXmlArgs args) {
        ChainAnalysisDTO chainAnalysis = args.getChainAnalysis();
        DesignMetaDTO designMeta = args.getDesignMeta();
        GenerateParamRetval generateParamRetval = args.getGenerateParamRetval();
        GenerateReturnTypeRetval generateReturnTypeRetval = args.getGenerateReturnTypeRetval();

        for (String mapperPath : designMeta.getMapperPaths()) {
            XmlSourceFile mapperXml = this.findMapperXml(mapperPath, args.getMethodAddedMapperXmls());
            if (mapperXml == null) {
                continue;
            }

            List<String> xmlLines = Lists.newArrayList();
            xmlLines.add("");
            if (chainAnalysis.getChainInitialMethod() == KeywordConstant.ChainInitialMethod.SELECT) {
                // QUERY
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = this.concatSelectStartTag(designMeta, chainAnalysis, generateParamRetval,
                        generateReturnTypeRetval);
                xmlLines.add(startTag);

                if (queryTransformerConfig.getEnableGenerateFormatterMarker()) {
                    xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
                }
                ArrayList<JoinClauseDTO> joinClauses = Lists.newArrayList(chainAnalysis.getJoinClauses());
                boolean join = !joinClauses.isEmpty();
                xmlLines.add(SINGLE_INDENT + "SELECT");
                if (chainAnalysis.getReturnShape() == ReturnShapeEnum.count) {
                    xmlLines.add(DOUBLE_INDENT + "COUNT(*)");
                } else if (CollectionUtils.isEmpty(chainAnalysis.getSelectProperties())) {
                    if (join) {
                        // 有join时，最外层的select_expr需要加上t1.
                        for (PropertyDTO property : designMeta.getProperties().values()) {
                            xmlLines.add(DOUBLE_INDENT + "t1.`" + property.getColumnName() + "` AS "
                                    + property.getPropertyName() + ",");
                        }
                        // 有join时，还需要select joinedProperties
                        for (int i = 0; i < joinClauses.size(); i++) {
                            JoinClauseDTO joinClause = joinClauses.get(i);
                            for (JoinedPropertyDTO joinedProp : joinClause.getJoinedProperties()) {
                                xmlLines.add(
                                        DOUBLE_INDENT + "t" + (i + 2) + ".`" + joinedProp.getProperty().getColumnName()
                                                + "` AS " + joinedProp.getVarName() + ",");
                            }
                        }
                    } else {
                        xmlLines.add(DOUBLE_INDENT + "<include refid='all' />");
                    }
                } else {
                    for (PropertyDTO property : chainAnalysis.getSelectProperties()) {
                        xmlLines.add(DOUBLE_INDENT + (join ? "t1." : "") + "`" + property.getColumnName() + "` AS "
                                + property.getPropertyName() + ",");
                    }
                    // 删除最后一个语句中，最后的逗号
                    int last = xmlLines.size() - 1;
                    xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                }
                xmlLines.add(SINGLE_INDENT + "FROM " + "`" + designMeta.getTableName() + "`" + (join ? " t1" : ""));
                for (int i = 0; i < joinClauses.size(); i++) {
                    xmlLines.addAll(concatJoinSection(joinClauses.get(i), i));
                }
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, true));
                if (CollectionUtils.isNotEmpty(chainAnalysis.getSortProperties())) {
                    xmlLines.add(SINGLE_INDENT + "ORDER BY");
                    for (SortPropertyDTO sortProp : chainAnalysis.getSortProperties()) {
                        PropertyDTO property = designMeta.getProperties().get(sortProp.getPropertyName());
                        xmlLines.add(DOUBLE_INDENT + (join ? "t1." : "") + "`" + property.getColumnName() + "`" + (
                                sortProp.getOrderSequence() == OrderSequenceEnum.DESC ? " DESC," : ","));
                    }
                    // 删除最后一个语句中，最后的逗号
                    int last = xmlLines.size() - 1;
                    xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                }
                if (chainAnalysis.getReturnShape() == ReturnShapeEnum.one) {
                    xmlLines.add(SINGLE_INDENT + "LIMIT 1");
                }

                if (queryTransformerConfig.getEnableGenerateFormatterMarker()) {
                    xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
                }
                xmlLines.add("</select>");
            } else if (chainAnalysis.getChainInitialMethod() == KeywordConstant.ChainInitialMethod.UPDATE) {
                // UPDATE
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = concatUpdateStartTag(chainAnalysis, generateParamRetval);
                xmlLines.add(startTag);
                if (queryTransformerConfig.getEnableGenerateFormatterMarker()) {
                    xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
                }
                xmlLines.add(SINGLE_INDENT + "UPDATE `" + designMeta.getTableName() + "`");
                xmlLines.add(SINGLE_INDENT + "SET");
                for (AssignmentDTO assignment : chainAnalysis.getAssignments()) {
                    PropertyDTO property = designMeta.getProperties().get(assignment.getProperty().getPropertyName());
                    xmlLines.add(
                            DOUBLE_INDENT + "`" + property.getColumnName() + "` = #{" + assignment.getVarName() + "},");
                }
                // 删除最后一个语句中，最后的逗号
                int last = xmlLines.size() - 1;
                xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, true));
                if (queryTransformerConfig.getEnableGenerateFormatterMarker()) {
                    xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
                }
                xmlLines.add("</update>");
            } else if (chainAnalysis.getChainInitialMethod() == KeywordConstant.ChainInitialMethod.DELETE) {
                // DROP
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = concatDeleteStartTag(chainAnalysis, generateParamRetval);
                xmlLines.add(startTag);
                if (CollectionUtils.isNotEmpty(chainAnalysis.getSearchConditions())) {
                    if (queryTransformerConfig.getEnableGenerateFormatterMarker()) {
                        xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_OFF_MARKER);
                    }
                }
                xmlLines.add(SINGLE_INDENT + "DELETE FROM `" + designMeta.getTableName() + "`");
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, false));
                if (CollectionUtils.isNotEmpty(chainAnalysis.getSearchConditions())) {
                    if (queryTransformerConfig.getEnableGenerateFormatterMarker()) {
                        xmlLines.add(SINGLE_INDENT + BaseConstant.FORMATTER_ON_MARKER);
                    }
                }
                xmlLines.add("</delete>");
            } else {
                throw new RuntimeException("impossible unless bug.");
            }

            List<String> newLines = Lists.newArrayList();
            List<String> lines = mapperXml.getContentLines();
            Collections.reverse(lines);
            for (String line : lines) {
                newLines.add(line);
                if (line.contains("</mapper>")) {
                    Collections.reverse(xmlLines);
                    for (String xmlLine : xmlLines) {
                        if (StringUtils.isNotBlank(xmlLine)) {
                            newLines.add(SINGLE_INDENT + xmlLine);
                        }
                    }
                    newLines.add("");
                }
            }
            Collections.reverse(newLines);

            // 作为本次queryChain的处理结果记录到map
            mapperXml.setContentLines(newLines);
        }
    }

    private List<String> concatJoinSection(JoinClauseDTO joinClause, int i) {
        i += 2;
        List<String> xmlLines = Lists.newArrayList();
        String joinSql = DOUBLE_INDENT + joinClause.getJoinType().getSql() + " `" + joinClause.getJoinedDesignMeta()
                .getTableName() + "` t" + i + " ON ";
        if (joinClause.getJoinConditions().size() == 1) {
            JoinConditionDTO joinCond = Iterables.getOnlyElement(joinClause.getJoinConditions());
            String onBinary = concatOnBinary(i, joinCond);
            xmlLines.add(joinSql + onBinary);
        } else {
            // join有多个joinCond时，每个joinCond占1行
            xmlLines.add(joinSql + "(");
            for (JoinConditionDTO joinCond : joinClause.getJoinConditions()) {
                xmlLines.add(TREBLE_INDENT + concatOnBinary(i, joinCond));
            }
            xmlLines.add(DOUBLE_INDENT + ")");
        }
        return xmlLines;
    }

    private static String concatOnBinary(int i, JoinConditionDTO joinCond) {
        String onBinary = "t" + i + ".`" + joinCond.getProperty().getColumnName() + "`";
        switch (joinCond.getComparisonOperator()) {
            case EQUALS:
                onBinary += " = ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1.`" + joinCond.getComparedProperty().getColumnName() + "`";
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case NOT_EQUALS:
                onBinary += " != ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1.`" + joinCond.getComparedProperty().getColumnName() + "`";
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case IN:
                // 只可能为argument
                onBinary += " IN (<foreach collection='" + joinCond.getVarName()
                        + "' item='one' separator=','>#{one}</foreach>)";
                break;
            case NOT_IN:
                // 只可能为argument
                onBinary += " NOT IN (<foreach collection='" + joinCond.getVarName()
                        + "' item='one' separator=','>#{one}</foreach>)";
                break;
            case GREATER_THEN:
                onBinary += " > ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1.`" + joinCond.getComparedProperty().getColumnName() + "`";
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case GREATER_OR_EQUALS:
                onBinary += " >= ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1.`" + joinCond.getComparedProperty().getColumnName() + "`";
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case LESS_THEN:
                onBinary += " < ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1.`" + joinCond.getComparedProperty().getColumnName() + "`";
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case LESS_OR_EQUALS:
                onBinary += " <= ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1.`" + joinCond.getComparedProperty().getColumnName() + "`";
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case NOT_NULL:
                // 只可能为argument
                onBinary += " IS NOT NULL";
                break;
            case IS_NULL:
                // 只可能为argument
                onBinary += " IS NULL";
                break;
            case LIKE:
                // 只可能为argument
                onBinary += " LIKE CONCAT('%', #{" + joinCond.getVarName() + "}, '%')";
                break;
        }
        return onBinary;
    }


    private ClassOrInterfaceDeclaration findMapper(String mapperQualifier,
            Map<String, ClassOrInterfaceDeclaration> methodAddedMappers) {
        // 尝试先从其他queryChain的处理结果中获取mapper
        if (methodAddedMappers.containsKey(mapperQualifier)) {
            return methodAddedMappers.get(mapperQualifier);
        }
        Optional<CompilationUnit> cu = AstForestContext.get().findCu(mapperQualifier);
        if (!cu.isPresent()) {
            return null;
        }
        Optional<TypeDeclaration<?>> pt = cu.get().getPrimaryType();
        if (!pt.isPresent()) {
            return null;
        }
        if (!pt.get().isClassOrInterfaceDeclaration()) {
            return null;
        }
        ClassOrInterfaceDeclaration mapper = pt.get().asClassOrInterfaceDeclaration();

        // 作为本次queryChain的处理结果记录到map
        methodAddedMappers.put(mapperQualifier, mapper);

        return mapper;
    }

    private XmlSourceFile findMapperXml(String mapperPath, Map<String, XmlSourceFile> methodAddedMapperXmls) {
        // 尝试先从其他queryChain的处理结果中获取mapperXml
        if (methodAddedMapperXmls.containsKey(mapperPath)) {
            return methodAddedMapperXmls.get(mapperPath);
        }

        File mapperXml = new File(mapperPath);
        if (!mapperXml.exists()) {
            return null;
        }
        XmlSourceFile result = new XmlSourceFile(mapperXml);

        // 作为本次queryChain的处理结果记录到map
        methodAddedMapperXmls.put(mapperPath, result);

        return result;
    }

    private List<String> concatWhereSection(DesignMetaDTO designMeta, ChainAnalysisDTO chainAnalysis,
            boolean needNotDeletedSql) {
        List<String> xmlLines = Lists.newArrayList();
        boolean join = !chainAnalysis.getJoinClauses().isEmpty();
        xmlLines.add(SINGLE_INDENT + "WHERE 1 = 1");
        if (needNotDeletedSql && designMeta.getNotDeletedSql() != null) {
            xmlLines.add(SINGLE_INDENT + "  AND " + designMeta.getNotDeletedSql());
        }
        for (SearchConditionDTO searchCond : chainAnalysis.getSearchConditions()) {
            PropertyDTO property = searchCond.getProperty();
            String varName = searchCond.getVarName();
            String dollarVar = "#{" + varName + "}";

            String ifTag = SINGLE_INDENT + "<if test=\"" + varName + " != null";
            if (property.getJavaType().getQualifier().equals("java.lang.String")) {
                ifTag += " and " + varName + " != ''";
            }
            ifTag += "\">";
            switch (searchCond.getComparisonOperator()) {
                case EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(
                                SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName() + "` = "
                                        + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(
                                DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName() + "` = "
                                        + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(
                                SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName() + "` != "
                                        + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(
                                DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName() + "` != "
                                        + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case IN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(SINGLE_INDENT + "<if test=\"" + varName + " != null\">");
                        xmlLines.add(DOUBLE_INDENT + "<if test=\"" + varName + ".size() > 0\">");
                        xmlLines.add(TREBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                        xmlLines.add(DOUBLE_INDENT + "</if>");
                        xmlLines.add(DOUBLE_INDENT + "<if test=\"" + varName + ".size() == 0\">");
                        xmlLines.add(TREBLE_INDENT + "AND 1 != 1");
                        xmlLines.add(DOUBLE_INDENT + "</if>");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_IN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` NOT IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(
                                SINGLE_INDENT + String.format("<if test=\"%s != null and %s.size() > 0\">", varName,
                                        varName));
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` NOT IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(
                                SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName() + "` > "
                                        + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(
                                DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName() + "` > "
                                        + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(
                                SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName() + "` >= "
                                        + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(
                                DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName() + "` >= "
                                        + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` &lt; " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` &lt; " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` &lt;= " + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` &lt;= " + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_NULL:
                    xmlLines.add(SINGLE_INDENT + "  AND " + (join ? "t1." : "") + "`" + property.getColumnName()
                            + "` IS NOT NULL");
                    break;
                case IS_NULL:
                    xmlLines.add(SINGLE_INDENT + "  AND " + (join ? "t1." : "") + "`" + property.getColumnName()
                            + "` IS NULL");
                    break;
                case LIKE:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` LIKE CONCAT('%', " + dollarVar + ", '%')");
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + "`" + property.getColumnName()
                                + "` LIKE CONCAT('%', " + dollarVar + ", '%')");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
            }
        }
        return xmlLines;
    }

    private String concatLotNoComment(ChainAnalysisDTO chainAnalysis) {
        if (commonConfig.getEnableLotNoAnnounce()) {
            return "<!-- " + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo() + " -->";
        }
        return "";
    }

    private String concatSelectStartTag(DesignMetaDTO designMeta, ChainAnalysisDTO chainAnalysis,
            GenerateParamRetval paramGeneration, GenerateReturnTypeRetval resultGeneration) {
        String startTag = "<select id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            Parameter onlyParam = paramGeneration.getParameters().get(0);
            if (onlyParam.getAnnotations().stream()
                    .noneMatch(a -> a.getNameAsString().equals("org.apache.ibatis.annotations.Param"))) {
                startTag += " parameterType='" + onlyParam.getTypeAsString() + "'";
            }
        }
        if (resultGeneration.getElementTypeQualifier() != null && !resultGeneration.getElementTypeQualifier()
                .equals(designMeta.getEntityQualifier())) {
            startTag += " resultType='" + resultGeneration.getElementTypeQualifier() + "'>";
        } else if (!resultGeneration.getResultType().equals(PrimitiveType.intType())) {
            startTag += " resultMap='all'>";
        } else {
            startTag += " resultType='int'>";
        }
        return startTag;
    }

    private String concatUpdateStartTag(ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration) {
        String startTag = "<update id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            startTag += " parameterType='" + paramGeneration.getParameters().get(0).getTypeAsString() + "'";
        }
        startTag += ">";
        return startTag;
    }

    private String concatDeleteStartTag(ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration) {
        String startTag = "<delete id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            startTag += " parameterType='" + paramGeneration.getParameters().get(0).getTypeAsString() + "'";
        }
        startTag += ">";
        return startTag;
    }

}