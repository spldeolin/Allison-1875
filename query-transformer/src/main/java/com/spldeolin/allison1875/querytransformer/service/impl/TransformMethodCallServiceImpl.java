package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.service.TransformMethodCallService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-09
 */
@Slf4j
@Singleton
public class TransformMethodCallServiceImpl implements TransformMethodCallService {

    @Override
    public String methodCallExpr(String mapperVarName, ChainAnalysisDto chainAnalysis,
            GenerateParamRetval paramGeneration) {
        String result = mapperVarName + "." + chainAnalysis.getMethodName() + "(";
        if (paramGeneration.getIsCond()) {
            String condQualifier = paramGeneration.getParameters().get(0).getTypeAsString();
            result += MoreStringUtils.toLowerCamel(MoreStringUtils.splitAndGetLastPart(condQualifier, "."));
        } else {
            Set<PhraseDto> phrases = chainAnalysis.getUpdatePhrases();
            phrases.addAll(chainAnalysis.getByPhrases());
            result += phrases.stream().filter(p -> p.getPredicate() != PredicateEnum.IS_NULL
                            && p.getPredicate() != PredicateEnum.NOT_NULL).map(p -> p.getObjectExpr().toString())
                    .collect(Collectors.joining(", "));
        }
        result += ")";
        log.info("Method Call built [{}]", result);

        return result;
    }

    @Override
    public List<Statement> argumentBuildStmts(ChainAnalysisDto chainAnalysis, GenerateParamRetval paramGeneration) {
        log.info("build Javabean setter call");
        String javabeanTypeQualifier = paramGeneration.getParameters().get(0).getTypeAsString();
        String javabeanVarName = MoreStringUtils.toLowerCamel(
                MoreStringUtils.splitAndGetLastPart(javabeanTypeQualifier, "."));
        List<Statement> result = Lists.newArrayList();
        result.add(StaticJavaParser.parseStatement(
                "final " + javabeanTypeQualifier + " " + javabeanVarName + " = new " + javabeanTypeQualifier + "();"));
        for (PhraseDto updatePhrase : chainAnalysis.getUpdatePhrases()) {
            result.add(StaticJavaParser.parseStatement(
                    javabeanVarName + ".set" + MoreStringUtils.toUpperCamel(updatePhrase.getVarName()) + "("
                            + updatePhrase.getObjectExpr() + ");"));
        }
        for (PhraseDto byPhrase : chainAnalysis.getByPhrases()) {
            if (Lists.newArrayList(PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL).contains(byPhrase.getPredicate())) {
                continue;
            }
            result.add(StaticJavaParser.parseStatement(
                    javabeanVarName + ".set" + MoreStringUtils.toUpperCamel(byPhrase.getVarName()) + "("
                            + byPhrase.getObjectExpr() + ");"));
        }
        return result;
    }

    @Override
    public List<Statement> mapOrMultimapBuildStmts(DesignMetaDto designMeta, ChainAnalysisDto chainAnalysis,
            GenerateReturnTypeRetval resultGeneration) {
        Map<String, PropertyDto> properties = designMeta.getProperties();

        if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.each) {
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

        if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.multiEach) {
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

    private String calcResultVarName(ChainAnalysisDto chainAnalysis) {
        String varName = chainAnalysis.getMethodName();
        if (chainAnalysis.getChain().getParentNode().filter(p -> p instanceof AssignExpr).isPresent()) {
            varName = ((AssignExpr) chainAnalysis.getChain().getParentNode().get()).getTarget().toString();
        }
        return varName;
    }

}