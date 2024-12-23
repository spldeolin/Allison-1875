package com.spldeolin.allison1875.common.service.impl;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Pair;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.dto.FieldArg;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-05-26
 */
@Singleton
@Slf4j
public class DataModelServiceImpl implements DataModelService {

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportExprService importExprService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public DataModelGeneration generateDataModel(DataModelArg arg) {
        String className = arg.getClassName();
        Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(arg.getAstForest().getSourceRoot(),
                arg.getPackageName(), className + ".java");

        if (absulutePath.toFile().exists()) {
            if (arg.getDataModelExistenceResolution() == FileExistenceResolutionEnum.OVERWRITE) {
                log.info("Entity [{}] is exist, use [overwrite] resolution", className);
            } else if (arg.getDataModelExistenceResolution() == FileExistenceResolutionEnum.RENAME) {
                String oldClassName = className;
                log.info("Entity [{}] is exist, use [rename] resolution", oldClassName);
                absulutePath = antiDuplicationService.getNewPathIfExist(absulutePath);
                className = FilenameUtils.getBaseName(absulutePath.toString());
            } else {
                throw new Allison1875Exception(
                        "unknown FileExistenceResolutionEnum [" + arg.getDataModelExistenceResolution() + "]");
            }
        }

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(absulutePath);
        cu.setPackageDeclaration(arg.getPackageName());

        ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
        coid.addAnnotation(annotationExprService.lombokData());
        coid.addAnnotation(annotationExprService.lombokAccessors());
        coid.addAnnotation(annotationExprService.lomokFieldDefaultsPrivate());
        coid.setPublic(true).setInterface(false).setName(className);
        if (arg.getIsDataModelSerializable()) {
            coid.addImplementedType("java.io.Serializable");
        }
        if (arg.getIsDataModelCloneable()) {
            coid.addImplementedType("Cloneable");
        }
        String comment = MoreObjects.firstNonNull(arg.getDescription(), "");
        JavadocUtils.setJavadoc(coid, comment, arg.getAuthor() + " " + LocalDate.now());
        cu.addType(coid);

        for (FieldArg fieldArg : arg.getFieldArgs()) {
            FieldDeclaration field = coid.addField(fieldArg.getTypeQualifier(), fieldArg.getFieldName());
            if (fieldArg.getDescription() != null) {
                field.setJavadocComment(fieldArg.getDescription());
            }
            // more for Field
            if (fieldArg.getMoreOperation() != null) {
                fieldArg.getMoreOperation().accept(coid, field);
            }
        }

        // more for DataModel
        if (arg.getMoreOperation() != null) {
            arg.getMoreOperation().accept(cu, coid);
        }

        if (arg.getIsDataModelSerializable()) {
            coid.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration(
                    "private static final long serialVersionUID = " + RandomUtils.nextLong() + "L;"));
        }
        if (arg.getIsDataModelCloneable()) {
            coid.getMembers().addLast(StaticJavaParser.parseBodyDeclaration(
                    "@Override public Object clone() throws CloneNotSupportedException { return super.clone(); }"));
        }

        importExprService.extractQualifiedTypeToImport(cu);

        DataModelGeneration result = new DataModelGeneration();
        result.setCu(cu);
        result.setFileFlush(FileFlush.build(cu));
        result.setDtoName(className);
        result.setDtoQualifier(arg.getPackageName() + "." + className);
        result.setCoid(coid);
        result.setPath(absulutePath);
        return result;
    }

    @Override
    public Map<String, CompilationUnit> collectNestDataModels(TypeDeclaration<?> td) {
        Map<String, CompilationUnit> result = Maps.newLinkedHashMap();
        for (FieldDeclaration field : td.getFields()) {
            ResolvedFieldDeclaration resolveField = field.resolve();
            analyzeField(resolveField, result);
        }
        return result;
    }

    private void analyzeField(ResolvedFieldDeclaration field, Map<String, CompilationUnit> collector) {
        ResolvedType type = field.getType();
        log.info("field={}", field);

        if (type.isReferenceType()) {
            String qualifier = type.asReferenceType().getQualifiedName();

            // 如果能找到源码……
            AstForestContext.get().tryFindCu(qualifier).flatMap(CompilationUnit::getPrimaryType).ifPresent(pt -> {
                CompilationUnit previousCu = collector.putIfAbsent(qualifier, pt.findCompilationUnit()
                        .orElseThrow(() -> new Allison1875Exception("cannot find cu for " + pt.getName())));
                if (previousCu == null) {
                    for (FieldDeclaration nestField : pt.getFields()) {
                        analyzeField(nestField.resolve(), collector);
                    }
                }
            });

            // 递归遍历type parameters
            analyzeType(type.asReferenceType(), collector);
        }
    }

    private void analyzeType(ResolvedReferenceType type, Map<String, CompilationUnit> collector) {
        log.info("type={}", type.describe());
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = type.getTypeParametersMap();
        List<ResolvedType> typeParameters = typeParametersMap.stream().map(pair -> pair.b).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(typeParameters)) {
            for (ResolvedType typeParamter : typeParameters) {
                if (typeParamter.isReferenceType()) {
                    String qualifier = typeParamter.asReferenceType().getQualifiedName();

                    // 如果能找到源码……
                    AstForestContext.get().tryFindCu(qualifier).flatMap(CompilationUnit::getPrimaryType)
                            .ifPresent(pt -> {
                                CompilationUnit previousCu = collector.putIfAbsent(qualifier, pt.findCompilationUnit()
                                        .orElseThrow(
                                                () -> new Allison1875Exception("cannot find cu for " + pt.getName())));
                                if (previousCu == null) {
                                    for (FieldDeclaration nestField : pt.getFields()) {
                                        analyzeField(nestField.resolve(), collector);
                                    }
                                }
                            });

                    // 递归遍历type parameters
                    analyzeType(typeParamter.asReferenceType(), collector);
                }
            }
        }
    }

}
