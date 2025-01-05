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
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.dto.FieldArg;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.KeywordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.JavaTypeNamingDTO;
import com.spldeolin.allison1875.querytransformer.dto.Binary;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.CompareableBinary;
import com.spldeolin.allison1875.querytransformer.dto.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.dto.VariableProperty;
import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnShapeEnum;
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
    private DataModelService dataModelGeneratorService;

    @Override
    public GenerateParamRetval generateParam(ChainAnalysisDTO chainAnalysis) {
        List<Parameter> params = Lists.newArrayList();
        boolean isParamDTO = false;
        FileFlush paramDTOFlush = null;

        Set<Binary> binaries = chainAnalysis.getBinariesAsArgs();
        if (binaries.size() > 3) {
            DataModelArg dataModelArg = new DataModelArg();
            dataModelArg.setAstForest(AstForestContext.get());
            dataModelArg.setPackageName(commonConfig.getParamDTOPackage());
            if (commonConfig.getEnableLotNoAnnounce()) {
                dataModelArg.setDescription(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo());
            }
            dataModelArg.setClassName(MoreStringUtils.toUpperCamel(chainAnalysis.getMethodName()) + "Param");
            dataModelArg.setAuthor(commonConfig.getAuthor());
            dataModelArg.setIsDataModelSerializable(commonConfig.getIsDataModelSerializable());
            dataModelArg.setIsDataModelCloneable(commonConfig.getIsDataModelCloneable());
            for (Binary binary : binaries) {
                String varName = binary.getVarName();
                JavaTypeNamingDTO javaType = binary.getProperty().getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(binary.getProperty().getDescription());
                if (binary instanceof CompareableBinary && Lists.newArrayList(ComparisonOperatorEnum.IN,
                        ComparisonOperatorEnum.NOT_IN).contains(((CompareableBinary) binary).getComparisonOperator())) {
                    fieldArg.setTypeQualifier("java.util.List<" + javaType.getQualifier() + ">");
                } else {
                    fieldArg.setTypeQualifier(javaType.getQualifier());
                }
                fieldArg.setFieldName(varName);
                dataModelArg.getFieldArgs().add(fieldArg);
            }
            dataModelArg.setDataModelExistenceResolution(FileExistenceResolutionEnum.RENAME);
            DataModelGeneration paramDTOGeneration = dataModelGeneratorService.generateDataModel(dataModelArg);
            paramDTOFlush = paramDTOGeneration.getFileFlush();
            Parameter param = new Parameter();
            param.setType(paramDTOGeneration.getDtoQualifier());
            param.setName(MoreStringUtils.toLowerCamel(paramDTOGeneration.getDtoName()));
            params.add(param);
            isParamDTO = true;
        } else if (CollectionUtils.isNotEmpty(binaries)) {
            for (Binary binary : binaries) {
                String varName = binary.getVarName();
                JavaTypeNamingDTO javaType = binary.getProperty().getJavaType();
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
            return new GenerateParamRetval().setIsParamDTO(false);
        }

        GenerateParamRetval result = new GenerateParamRetval();
        result.getParameters().addAll(params);
        result.setIsParamDTO(isParamDTO);
        result.setParamDTOFlush(paramDTOFlush);
        return result;
    }

    @Override
    public GenerateReturnTypeRetval generateReturnType(ChainAnalysisDTO chainAnalysis) {
        boolean isAssigned = isAssigned(chainAnalysis);
        GenerateReturnTypeRetval result = new GenerateReturnTypeRetval();

        if (Lists.newArrayList(KeywordConstant.ChainInitialMethod.UPDATE, KeywordConstant.ChainInitialMethod.DELETE)
                .contains(chainAnalysis.getChainInitialMethod())) {
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
            DataModelArg dataModelArg = new DataModelArg();
            dataModelArg.setAstForest(AstForestContext.get());
            dataModelArg.setPackageName(commonConfig.getRecordDTOPackage());
            if (commonConfig.getEnableLotNoAnnounce()) {
                dataModelArg.setDescription(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo());
            }
            dataModelArg.setClassName(MoreStringUtils.toUpperCamel(chainAnalysis.getMethodName()) + "Record");
            dataModelArg.setAuthor(commonConfig.getAuthor());
            dataModelArg.setIsDataModelSerializable(commonConfig.getIsDataModelSerializable());
            dataModelArg.setIsDataModelCloneable(commonConfig.getIsDataModelCloneable());
            for (VariableProperty returnProp : returnProps) {
                JavaTypeNamingDTO javaType = returnProp.getProperty().getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setDescription(returnProp.getProperty().getDescription());
                fieldArg.setTypeQualifier(javaType.getQualifier());
                fieldArg.setFieldName(returnProp.getVarName());
                dataModelArg.getFieldArgs().add(fieldArg);
            }
            dataModelArg.setDataModelExistenceResolution(FileExistenceResolutionEnum.RENAME);
            DataModelGeneration recordDTOGeneration = dataModelGeneratorService.generateDataModel(dataModelArg);
            result.setFlush(recordDTOGeneration.getFileFlush());
            result.setElementTypeQualifier(recordDTOGeneration.getDtoQualifier());
            if (Lists.newArrayList(ReturnShapeEnum.many, ReturnShapeEnum.each, ReturnShapeEnum.multiEach)
                    .contains(chainAnalysis.getReturnShape())) {
                result.setResultType(
                        StaticJavaParser.parseType("java.util.List<" + recordDTOGeneration.getDtoQualifier() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(recordDTOGeneration.getDtoQualifier()));
            }
            return result;

        } else if (returnProps.size() == 1) {
            // 指定了1个属性，使用该属性类型作为返回值类型
            VariableProperty returnProp = Iterables.getOnlyElement(returnProps);
            JavaTypeNamingDTO javaType = returnProp.getProperty().getJavaType();
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

    private boolean isAssigned(ChainAnalysisDTO chainAnalysis) {
        if (chainAnalysis.getChain().getParentNode().isPresent()) {
            return chainAnalysis.getChain().getParentNode().get().getParentNode()
                    .filter(parent -> parent instanceof VariableDeclarationExpr).isPresent();
        }
        return false;
    }

}