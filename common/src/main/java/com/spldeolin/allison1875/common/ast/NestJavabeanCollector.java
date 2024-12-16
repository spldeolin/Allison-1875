package com.spldeolin.allison1875.common.ast;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 递归收集一个Javabean下通过Field类型引入所有Nest Javabean
 *
 * @author Deolin 2024-08-17
 */
@Slf4j
public class NestJavabeanCollector {

    private final Map<String, CompilationUnit> cus = Maps.newLinkedHashMap();

    private final AstForest astForest;

    private final TypeDeclaration<?> td;

    public NestJavabeanCollector(AstForest astForest, TypeDeclaration<?> td) {
        this.astForest = astForest;
        this.td = td;
    }

    public void collect() {
        for (FieldDeclaration field : td.getFields()) {
            ResolvedFieldDeclaration resolveField = field.resolve();
            analyzeField(resolveField);
        }
    }

    private void analyzeField(ResolvedFieldDeclaration field) {
        ResolvedType type = field.getType();
        log.info("field={}", field);

        if (type.isReferenceType()) {
            String qualifier = type.asReferenceType().getQualifiedName();

            // 如果能找到源码……
            astForest.tryFindCu(qualifier).flatMap(CompilationUnit::getPrimaryType).ifPresent(pt -> {
                CompilationUnit previousCu = cus.putIfAbsent(qualifier, pt.findCompilationUnit()
                        .orElseThrow(() -> new Allison1875Exception("cannot find cu for " + pt.getName())));
                if (previousCu == null) {
                    for (FieldDeclaration nestField : pt.getFields()) {
                        analyzeField(nestField.resolve());
                    }
                }
            });

            // 递归遍历type parameters
            analyzeType(type.asReferenceType());
        }
    }

    private void analyzeType(ResolvedReferenceType type) {
        log.info("type={}", type.describe());
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = type.getTypeParametersMap();
        List<ResolvedType> typeParameters = typeParametersMap.stream().map(pair -> pair.b).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(typeParameters)) {
            for (ResolvedType typeParamter : typeParameters) {
                if (typeParamter.isReferenceType()) {
                    String qualifier = typeParamter.asReferenceType().getQualifiedName();

                    // 如果能找到源码……
                    astForest.tryFindCu(qualifier).flatMap(CompilationUnit::getPrimaryType).ifPresent(pt -> {
                        CompilationUnit previousCu = cus.putIfAbsent(qualifier, pt.findCompilationUnit()
                                .orElseThrow(() -> new Allison1875Exception("cannot find cu for " + pt.getName())));
                        if (previousCu == null) {
                            for (FieldDeclaration nestField : pt.getFields()) {
                                analyzeField(nestField.resolve());
                            }
                        }
                    });

                    // 递归遍历type parameters
                    analyzeType(typeParamter.asReferenceType());
                }
            }
        }
    }

    public Set<String> getTypeQualfiers() {
        return cus.keySet();
    }

    public List<CompilationUnit> getCus() {
        return Lists.newArrayList(cus.values());
    }

}
