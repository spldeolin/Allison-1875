package com.spldeolin.allison1875.startransformer.service.impl;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.startransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDTO;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDTO;
import com.spldeolin.allison1875.startransformer.service.StarChainService;
import com.spldeolin.allison1875.startransformer.util.NamingUtils;
import com.spldeolin.allison1875.support.StarSchema;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-05-12
 */
@Singleton
@Slf4j
public class StarChainServiceImpl implements StarChainService {

    @Inject
    private StarTransformerConfig config;

    @Override
    public List<MethodCallExpr> detectStarChains(BlockStmt block) {
        List<MethodCallExpr> mces = Lists.newArrayList();
        for (MethodCallExpr mce : block.findAll(MethodCallExpr.class)) {
            if ("over".equals(mce.getNameAsString()) && mce.getParentNode().isPresent()) {
                if (this.finalNameExprRecursively(mce, StarSchema.class.getName())) {
                    mces.add(mce);
                }
            }
        }
        return mces;
    }

    private boolean finalNameExprRecursively(MethodCallExpr mce, String untilNameExprMatchedQualifier) {
        Optional<Expression> scope = mce.getScope();
        if (scope.isPresent()) {
            if (scope.get().isMethodCallExpr()) {
                return finalNameExprRecursively(scope.get().asMethodCallExpr(), untilNameExprMatchedQualifier);
            }
            if (scope.get().isNameExpr()) {
                NameExpr nameExpr = scope.get().asNameExpr();
                try {
                    String describe = nameExpr.calculateResolvedType().describe();
                    if (untilNameExprMatchedQualifier.equals(describe)) {
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }


    @Override
    public ChainAnalysisDTO analyzeStarChain(MethodCallExpr starChain) throws IllegalChainException {
        return this.analyzeRecursively(starChain, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());
    }

    private ChainAnalysisDTO analyzeRecursively(MethodCallExpr mce, List<PhraseDTO> phrases, List<String> keys,
            List<String> mkeys) throws IllegalChainException {
        if (ChainMethodEnum.oo.toString().equals(mce.getNameAsString())) {
            PhraseDTO phrase = new PhraseDTO();
            phrase.setIsOneToOne(true);
            FieldAccessExpr fae = mce.getArgument(0).asFieldAccessExpr();
            phrase.setDtEntityQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("E").get().describe());
            phrase.setDtEntityName(NamingUtils.qualifierToTypeName(phrase.getDtEntityQualifier()));
            phrase.setDtDesignName(fae.getScope().toString());
            phrase.setFk(fae.getNameAsString());
            phrase.setFkTypeQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("K").get().describe());
            // One to One的维度表无法指定任何key，因为只有一条数据，没有意义
            phrase.setKeys(Lists.newArrayList());
            phrase.setMkeys(Lists.newArrayList());
            phrases.add(phrase);
        }
        if (ChainMethodEnum.om.toString().equals(mce.getNameAsString())) {
            PhraseDTO phrase = new PhraseDTO();
            phrase.setIsOneToOne(false);
            FieldAccessExpr fae = mce.getArgument(0).asFieldAccessExpr();
            phrase.setDtEntityQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("E").get().describe());
            phrase.setDtEntityName(NamingUtils.qualifierToTypeName(phrase.getDtEntityQualifier()));
            phrase.setDtDesignName(fae.getScope().toString());
            phrase.setFk(fae.getNameAsString());
            phrase.setFkTypeQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("K").get().describe());
            // 递归到此时，收集到的keys和mkeys均属于这个dt，组装完毕后需要清空并重新收集
            phrase.setKeys(Lists.newArrayList(keys));
            phrase.setMkeys(Lists.newArrayList(mkeys));
            if (CollectionUtils.isNotEmpty(phrase.getKeys()) || CollectionUtils.isNotEmpty(phrase.getMkeys())) {
                AstForestContext.get().findCu(phrase.getDtEntityQualifier()).ifPresent(cu -> {
                    for (VariableDeclarator vd : cu.findAll(VariableDeclarator.class)) {
                        phrase.getEntityFieldTypesEachFieldName()
                                .put(vd.getNameAsString(), vd.getType().resolve().describe());
                    }
                });
            }
            phrases.add(phrase);
            keys.clear();
            mkeys.clear();
        }
        if (ChainMethodEnum.key.toString().equals(mce.getNameAsString())) {
            keys.add(mce.getArgument(0).asFieldAccessExpr().getNameAsString());
        }
        if (ChainMethodEnum.mkey.toString().equals(mce.getNameAsString())) {
            mkeys.add(mce.getArgument(0).asFieldAccessExpr().getNameAsString());
        }
        if (ChainMethodEnum.cft.toString().equals(mce.getNameAsString())) {
            FieldAccessExpr fae = mce.getArgument(0).asFieldAccessExpr();
            ChainAnalysisDTO analysis = new ChainAnalysisDTO();
            analysis.setCftEntityQualifier(
                    fae.resolve().getType().asReferenceType().getGenericParameterByName("E").get().describe());
            analysis.setCftEntityName(NamingUtils.qualifierToTypeName(analysis.getCftEntityQualifier()));
            analysis.setCftDesignName(fae.getScope().toString());
            analysis.setCftDesignQualifier(fae.getScope().calculateResolvedType().describe());
            analysis.setCftSecondArgument(mce.getArgument(1));
            analysis.setPhrases(phrases);
            String wholeDTOName = this.buildWholeDTOName(analysis.getCftEntityName());
            analysis.setWholeDTOName(wholeDTOName);
            String hash = StringUtils.upperCase(HashingUtils.hashString(analysis.toString()));
            analysis.setLotNo(String.format("ST%s-%s", Allison1875.SHORT_VERSION, hash));
            return analysis;
        }
        if (mce.getScope().filter(Expression::isMethodCallExpr).isPresent()) {
            return this.analyzeRecursively(mce.getScope().get().asMethodCallExpr(), phrases, keys, mkeys);
        }
        throw new IllegalChainException("impossible unless bug.");
    }

    private String buildWholeDTOName(String entityName) {
        if (entityName.endsWith("Entity")) {
            return MoreStringUtils.replaceLast(entityName, "Entity", config.getWholeDTONamePostfix());
        } else {
            return entityName + config.getWholeDTONamePostfix();
        }
    }

}