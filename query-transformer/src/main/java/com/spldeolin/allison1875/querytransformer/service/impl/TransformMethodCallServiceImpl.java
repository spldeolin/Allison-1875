package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.exception.ParentAbsentException;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDTO;
import com.spldeolin.allison1875.querytransformer.enums.ReturnShapeEnum;
import com.spldeolin.allison1875.querytransformer.javabean.Binary;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.service.TransformMethodCallService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-09
 */
@Slf4j
@Singleton
public class TransformMethodCallServiceImpl implements TransformMethodCallService {

    @Override
    public String methodCallExpr(String mapperVarName, ChainAnalysisDTO chainAnalysis,
            GenerateParamRetval paramGeneration) {
        String result = mapperVarName + "." + chainAnalysis.getMethodName() + "(";
        if (paramGeneration.getIsCond()) {
            String condQualifier = paramGeneration.getParameters().get(0).getTypeAsString();
            result += MoreStringUtils.toLowerCamel(MoreStringUtils.splitAndGetLastPart(condQualifier, "."));
        } else {
            result += chainAnalysis.getBinariesAsArgs().stream().filter(b -> b.getArgument() != null)
                    .map(p -> p.getArgument().toString())
                    .collect(Collectors.joining(", "));
        }
        result += ")";
        log.info("Method Call built [{}]", result);

        return result;
    }

    @Override
    public List<Statement> argumentBuildStmts(ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration) {
        log.info("build Javabean setter call");
        String javabeanTypeQualifier = paramGeneration.getParameters().get(0).getTypeAsString();
        String javabeanVarName = MoreStringUtils.toLowerCamel(
                MoreStringUtils.splitAndGetLastPart(javabeanTypeQualifier, "."));
        List<Statement> result = Lists.newArrayList();
        result.add(StaticJavaParser.parseStatement(
                "final " + javabeanTypeQualifier + " " + javabeanVarName + " = new " + javabeanTypeQualifier + "();"));
        for (Binary binariesAsArg : chainAnalysis.getBinariesAsArgs()) {
            result.add(StaticJavaParser.parseStatement(
                    javabeanVarName + ".set" + MoreStringUtils.toUpperCamel(binariesAsArg.getVarName()) + "("
                            + binariesAsArg.getArgument() + ");"));
        }
        return result;
    }

    @Override
    public List<Statement> mapOrMultimapBuildStmts(DesignMetaDTO designMeta, ChainAnalysisDTO chainAnalysis,
            GenerateReturnTypeRetval resultGeneration) {
        Map<String, PropertyDTO> properties = designMeta.getProperties();

        if (chainAnalysis.getReturnShape() == ReturnShapeEnum.each) {
            String propertyName = chainAnalysis.getChain().getArgument(0).asFieldAccessExpr().getNameAsString();
            String propertyTypeName = properties.get(propertyName).getJavaType().getSimpleName();
            String elementTypeName = StringUtils.substringAfterLast(resultGeneration.getElementTypeQualifier(), ".");

            boolean isAssignWithoutType = (chainAnalysis.getChain().getParentNode().get().getParentNode()
                    .filter(gp -> gp instanceof ExpressionStmt)).isPresent();

            List<Statement> statements = Lists.newArrayList();
            if (isAssignWithoutType) {
                statements.add(
                        StaticJavaParser.parseStatement(calcResultVarName(chainAnalysis) + " = new HashMap<>();"));
            } else {
                statements.add(StaticJavaParser.parseStatement(
                        "final java.util.Map<" + propertyTypeName + ", " + elementTypeName + "> " + calcResultVarName(
                                chainAnalysis) + " = new HashMap<>();"));
            }
            statements.add(StaticJavaParser.parseStatement(
                    chainAnalysis.getMethodName() + "List.forEach(one -> " + calcResultVarName(chainAnalysis)
                            + ".put(one.get" + MoreStringUtils.toUpperCamel(propertyName) + "(), one));"));
            return statements;
        }

        if (chainAnalysis.getReturnShape() == ReturnShapeEnum.multiEach) {
            String propertyName = chainAnalysis.getChain().getArgument(0).asFieldAccessExpr().getNameAsString();
            String propertyTypeName = properties.get(propertyName).getJavaType().getSimpleName();
            String elementTypeName = StringUtils.substringAfterLast(resultGeneration.getElementTypeQualifier(), ".");

            boolean isAssignWithoutType = (chainAnalysis.getChain().getParentNode()
                    .orElseThrow(() -> new ParentAbsentException(chainAnalysis.getChain())).getParentNode()
                    .filter(gp -> gp instanceof ExpressionStmt)).isPresent();

            List<Statement> statements = Lists.newArrayList();
            if (isAssignWithoutType) {
                statements.add(StaticJavaParser.parseStatement(
                        calcResultVarName(chainAnalysis) + " = ArrayListMultimap.create();"));
            } else {
                statements.add(StaticJavaParser.parseStatement(
                        "final com.google.common.collect.ArrayListMultimap<" + propertyTypeName + ", " + elementTypeName
                                + "> " + calcResultVarName(chainAnalysis) + " = ArrayListMultimap.create();"));
            }
            statements.add(StaticJavaParser.parseStatement(
                    chainAnalysis.getMethodName() + "List.forEach(one -> " + calcResultVarName(chainAnalysis)
                            + ".put(one.get" + MoreStringUtils.toUpperCamel(propertyName) + "(), one));"));
            return statements;
        }

        return null;
    }

    private String calcResultVarName(ChainAnalysisDTO chainAnalysis) {
        String varName = chainAnalysis.getMethodName();
        if (chainAnalysis.getChain().getParentNode().filter(p -> p instanceof AssignExpr).isPresent()) {
            varName = ((AssignExpr) chainAnalysis.getChain().getParentNode().get()).getTarget().toString();
        }
        return varName;
    }

}