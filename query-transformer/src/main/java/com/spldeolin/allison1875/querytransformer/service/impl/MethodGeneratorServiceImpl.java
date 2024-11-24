package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.Set;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.FieldArg;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnShapeEnum;
import com.spldeolin.allison1875.querytransformer.javabean.Binary;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.CompareableBinary;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.javabean.VariableProperty;
import com.spldeolin.allison1875.querytransformer.service.MethodGeneratorService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Slf4j
public class MethodGeneratorServiceImpl implements MethodGeneratorService {

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Override
    public GenerateParamRetval generateParam(ChainAnalysisDto chainAnalysis) {
        List<Parameter> params = Lists.newArrayList();
        boolean isJavabean = false;
        FileFlush condFlush = null;

        Set<Binary> binaries = chainAnalysis.getBinariesAsArgs();
        if (binaries.size() > 3) {
            JavabeanArg javabeanArg = new JavabeanArg();
            javabeanArg.setAstForest(AstForestContext.get());
            javabeanArg.setPackageName(commonConfig.getCondPackage());
            if (commonConfig.getEnableLotNoAnnounce()) {
                javabeanArg.setDescription(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo());
            }
            javabeanArg.setClassName(MoreStringUtils.toUpperCamel(chainAnalysis.getMethodName()) + "Cond");
            javabeanArg.setAuthor(commonConfig.getAuthor());
            javabeanArg.setIsJavabeanSerializable(commonConfig.getIsJavabeanSerializable());
            javabeanArg.setIsJavabeanCloneable(commonConfig.getIsJavabeanCloneable());
            for (Binary binary : binaries) {
                String varName = binary.getVarName();
                JavaTypeNamingDto javaType = binary.getProperty().getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(binary.getProperty().getDescription());
                if (binary instanceof CompareableBinary && Lists.newArrayList(ComparisonOperatorEnum.IN,
                        ComparisonOperatorEnum.NOT_IN).contains(((CompareableBinary) binary).getComparisonOperator())) {
                    fieldArg.setTypeQualifier("java.util.List<" + javaType.getQualifier() + ">");
                } else {
                    fieldArg.setTypeQualifier(javaType.getQualifier());
                }
                fieldArg.setFieldName(varName);
                javabeanArg.getFieldArgs().add(fieldArg);
            }
            javabeanArg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration condGeneration = javabeanGeneratorService.generate(javabeanArg);
            condFlush = condGeneration.getFileFlush();
            Parameter param = new Parameter();
            param.setType(condGeneration.getJavabeanQualifier());
            param.setName(MoreStringUtils.toLowerCamel(condGeneration.getJavabeanName()));
            params.add(param);
            isJavabean = true;
        } else if (CollectionUtils.isNotEmpty(binaries)) {
            for (Binary binary : binaries) {
                String varName = binary.getVarName();
                JavaTypeNamingDto javaType = binary.getProperty().getJavaType();
                Parameter param = new Parameter();
                param.addAnnotation(StaticJavaParser.parseAnnotation(
                        String.format("@org.apache.ibatis.annotations.Param(\"%s\")", varName)));

                if (binary instanceof CompareableBinary && Lists.newArrayList(ComparisonOperatorEnum.IN,
                        ComparisonOperatorEnum.NOT_IN).contains(((CompareableBinary) binary).getComparisonOperator())) {
                    param.setType("java.util.List<" + javaType.getQualifier() + ">");
                } else {
                    param.setType(javaType.getQualifier());
                }
                param.setName(varName);
                params.add(param);
            }
        } else {
            return new GenerateParamRetval().setIsCond(false);
        }

        GenerateParamRetval result = new GenerateParamRetval();
        result.getParameters().addAll(params);
        result.setIsCond(isJavabean);
        result.setCondFlush(condFlush);
        return result;
    }

    @Override
    public GenerateReturnTypeRetval generateReturnType(ChainAnalysisDto chainAnalysis) {
        boolean isAssigned = isAssigned(chainAnalysis);
        GenerateReturnTypeRetval result = new GenerateReturnTypeRetval();

        if (Lists.newArrayList(ChainMethodEnum.update, ChainMethodEnum.drop).contains(chainAnalysis.getChainMethod())) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (chainAnalysis.getReturnShape() == ReturnShapeEnum.count) {
            result.setResultType(PrimitiveType.intType());
            return result;
        }

        if (isAssigned) {
            if (Lists.newArrayList(ReturnShapeEnum.many, ReturnShapeEnum.each, ReturnShapeEnum.multiEach)
                    .contains(chainAnalysis.getReturnShape())) {
                result.setResultType(
                        StaticJavaParser.parseType("java.util.List<" + chainAnalysis.getEntityQualifier() + ">"));
                result.setElementTypeQualifier(chainAnalysis.getEntityQualifier());
            } else {
                result.setResultType(StaticJavaParser.parseType(chainAnalysis.getEntityQualifier()));
                result.setElementTypeQualifier(chainAnalysis.getEntityQualifier());
            }
            return result;
        }

        Set<VariableProperty> returnProps = chainAnalysis.getPropertiesAsResult();
        if (returnProps.size() > 1) {
            JavabeanArg javabeanArg = new JavabeanArg();
            javabeanArg.setAstForest(AstForestContext.get());
            javabeanArg.setPackageName(commonConfig.getRecordPackage());
            if (commonConfig.getEnableLotNoAnnounce()) {
                javabeanArg.setDescription(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo());
            }
            javabeanArg.setClassName(MoreStringUtils.toUpperCamel(chainAnalysis.getMethodName()) + "Record");
            javabeanArg.setAuthor(commonConfig.getAuthor());
            javabeanArg.setIsJavabeanSerializable(commonConfig.getIsJavabeanSerializable());
            javabeanArg.setIsJavabeanCloneable(commonConfig.getIsJavabeanCloneable());
            for (VariableProperty returnProp : returnProps) {
                JavaTypeNamingDto javaType = returnProp.getProperty().getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(returnProp.getProperty().getDescription());
                fieldArg.setTypeQualifier(javaType.getQualifier());
                fieldArg.setFieldName(returnProp.getVarName());
                javabeanArg.getFieldArgs().add(fieldArg);
            }
            javabeanArg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration recordGeneration = javabeanGeneratorService.generate(javabeanArg);
            result.setFlush(recordGeneration.getFileFlush());
            result.setElementTypeQualifier(recordGeneration.getJavabeanQualifier());
            if (Lists.newArrayList(ReturnShapeEnum.many, ReturnShapeEnum.each, ReturnShapeEnum.multiEach)
                    .contains(chainAnalysis.getReturnShape())) {
                result.setResultType(
                        StaticJavaParser.parseType("java.util.List<" + recordGeneration.getJavabeanQualifier() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(recordGeneration.getJavabeanQualifier()));
            }
            return result;

        } else if (returnProps.size() == 1) {
            // 指定了1个属性，使用该属性类型作为返回值类型
            VariableProperty returnProp = Iterables.getOnlyElement(returnProps);
            JavaTypeNamingDto javaType = returnProp.getProperty().getJavaType();
            result.setElementTypeQualifier(javaType.getQualifier());
            if (Lists.newArrayList(ReturnShapeEnum.many, ReturnShapeEnum.each, ReturnShapeEnum.multiEach)
                    .contains(chainAnalysis.getReturnShape())) {
                result.setResultType(StaticJavaParser.parseType("java.util.List<" + javaType.getQualifier() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(javaType.getQualifier()));
            }
            return result;

        } else {
            // 没有指定属性，使用Entity作为返回值类型
            result.setElementTypeQualifier(chainAnalysis.getEntityQualifier());
            if (Lists.newArrayList(ReturnShapeEnum.many, ReturnShapeEnum.each, ReturnShapeEnum.multiEach)
                    .contains(chainAnalysis.getReturnShape())) {
                result.setResultType(
                        StaticJavaParser.parseType("java.util.List<" + chainAnalysis.getEntityQualifier() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(chainAnalysis.getEntityQualifier()));
            }
            return result;
        }
    }

    private boolean isAssigned(ChainAnalysisDto chainAnalysis) {
        if (chainAnalysis.getChain().getParentNode().isPresent()) {
            return chainAnalysis.getChain().getParentNode().get().getParentNode()
                    .filter(parent -> parent instanceof VariableDeclarationExpr).isPresent();
        }
        return false;
    }

}