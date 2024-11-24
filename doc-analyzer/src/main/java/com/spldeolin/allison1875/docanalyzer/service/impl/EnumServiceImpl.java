package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeEnumConstantsRetval;
import com.spldeolin.allison1875.docanalyzer.service.EnumService;
import com.spldeolin.allison1875.docanalyzer.util.LoadClassUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-26
 */
@Singleton
@Slf4j
public class EnumServiceImpl implements EnumService {

    @Override
    public List<AnalyzeEnumConstantsRetval> analyzeEnumConstants(VariableDeclarator fieldVar) {
        ResolvedType resolvedType;
        try {
            resolvedType = fieldVar.getType().resolve();
        } catch (Exception e) {
            log.info("Type of FieldVar '{}' cannot resolve", fieldVar);
            return Lists.newArrayList();
        }

        return analyzeEnumConstants(resolvedType);
    }

    private List<AnalyzeEnumConstantsRetval> analyzeEnumConstants(ResolvedType resolvedType) {
        if (resolvedType.isArray()) {
            ResolvedArrayType arrayType = resolvedType.asArrayType();
            return this.analyzeEnumConstants(arrayType.getComponentType());
        }

        if (!resolvedType.isReferenceType()) {
            return Lists.newArrayList();
        }
        ResolvedReferenceType referenceType = resolvedType.asReferenceType();

        if (!referenceType.getTypeDeclaration().isPresent()) {
            return Lists.newArrayList();
        }
        ResolvedReferenceTypeDeclaration referenceTypeDeclaration = referenceType.getTypeDeclaration().get();

        if (referenceTypeDeclaration.getAncestors().stream()
                .anyMatch(ancestor -> ancestor.getId().equals("java.util.Collection"))
                && referenceType.getTypeParametersMap().size() == 1) {
            ResolvedType typeParameterOfCollection = referenceType.getTypeParametersMap().get(0).b;
            return this.analyzeEnumConstants(typeParameterOfCollection);
        }

        if (!referenceTypeDeclaration.isEnum()) {
            return Lists.newArrayList();
        }

        Class<?> enumClass;
        try {
            enumClass = LoadClassUtils.loadClass(referenceTypeDeclaration.getQualifiedName(),
                    AstForestContext.get().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.warn("class '{}' not found", referenceTypeDeclaration.getQualifiedName(), e);
            return Lists.newArrayList();
        }

        List<AnalyzeEnumConstantsRetval> result = Lists.newArrayList();
        for (Object enumConstant : enumClass.getEnumConstants()) {
            AnalyzeEnumConstantsRetval aecRetval = analyzeEnumConstant(enumConstant);
            if (aecRetval != null) {
                result.add(aecRetval);
            }
        }
        return result;
    }

    protected AnalyzeEnumConstantsRetval analyzeEnumConstant(Object enumConstant) {
        return null;
    }

}