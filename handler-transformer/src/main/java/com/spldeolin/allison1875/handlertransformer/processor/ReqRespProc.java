package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.builder.JavabeanCuBuilder;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.handle.FieldHandle;
import com.spldeolin.allison1875.handlertransformer.handle.javabean.BeforeJavabeanCuBuildResult;
import com.spldeolin.allison1875.handlertransformer.javabean.FirstLineDto;
import com.spldeolin.allison1875.handlertransformer.javabean.ReqDtoRespDtoInfo;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class ReqRespProc {

    @Inject
    private HandlerTransformerConfig handlerTransformerConfig;

    @Inject
    private FieldHandle fieldHandle;

    @Inject
    private EnsureNoRepeatProc ensureNoRepeatProc;

    public void checkInitBody(BlockStmt initBody, FirstLineDto firstLineDto) {
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 2) {
            throw new IllegalArgumentException(
                    "构造代码块下最多只能有2个类声明，分别用于代表ReqDto和RespDto。[" + firstLineDto + "] 当前："
                            + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                            .map(one -> one.getClassDeclaration().getNameAsString()).collect(Collectors.joining("、")));
        }
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 0) {
            for (LocalClassDeclarationStmt lcds : initBody.findAll(LocalClassDeclarationStmt.class)) {
                if (!StringUtils.equalsAnyIgnoreCase(lcds.getClassDeclaration().getNameAsString(), "Req", "Resp")) {
                    throw new IllegalArgumentException(
                            "构造代码块下类的命名只能是「Req」或者「Resp」。[" + firstLineDto + "] 当前：" + initBody.findAll(
                                            LocalClassDeclarationStmt.class).stream()
                                    .map(one -> one.getClassDeclaration().getNameAsString())
                                    .collect(Collectors.joining("、")));
                }
            }
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Req")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Req类。[" + firstLineDto + "]");
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Resp")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Resp类。[" + firstLineDto + "]");
        }
    }

    public ReqDtoRespDtoInfo createJavabeans(AstForest astForest, CompilationUnit cu, FirstLineDto firstLineDto,
            List<ClassOrInterfaceDeclaration> dtos) {
        ReqDtoRespDtoInfo result = new ReqDtoRespDtoInfo();
        Collection<JavabeanCuBuilder<JavabeanTypeEnum>> builders = Lists.newArrayList();
        Collection<String> dtoQualifiers = Lists.newArrayList();
        // 生成ReqDto、RespDto、NestDto
        for (ClassOrInterfaceDeclaration dto : dtos) {
            JavabeanCuBuilder<JavabeanTypeEnum> builder = new JavabeanCuBuilder<>();
            builder.sourceRoot(Locations.getStorage(cu).getSourceRoot());
            JavabeanTypeEnum javabeanType;
            javabeanType = estimateJavabeanType(dto);
            builder.context(javabeanType);

            String javabeanName = standardizeJavabeanName(firstLineDto, dto, javabeanType);
            javabeanName = ensureNoRepeatProc.inAstForest(astForest, javabeanName);
            dto.setName(javabeanName);

            // 计算每一个dto的package
            String pkg;
            if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
                pkg = handlerTransformerConfig.getReqDtoPackage();
            } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
                pkg = handlerTransformerConfig.getRespDtoPackage();
            } else if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
                pkg = handlerTransformerConfig.getReqNestDtoPackage();
                if (pkg == null) {
                    pkg = handlerTransformerConfig.getReqDtoPackage() + ".dto";
                }
            } else {
                pkg = handlerTransformerConfig.getRespNestDtoPackage();
                if (pkg == null) {
                    pkg = handlerTransformerConfig.getRespDtoPackage() + ".dto";
                }
            }
            builder.packageDeclaration(pkg);
            builder.importDeclarations(cu.getImports());
            builder.importDeclarationsString(
                    Lists.newArrayList(AnnotationConstant.VALID_QUALIFIER, "java.util.Collection",
                            AnnotationConstant.DATA_QUALIFIER, AnnotationConstant.ACCESSORS_QUALIFIER,
                            AnnotationConstant.FIELD_DEFAULTS_QUALIFIER, AnnotationConstant.ACCESS_LEVEL_QUALIFIER,
                            handlerTransformerConfig.getPageTypeQualifier()));
            ClassOrInterfaceDeclaration clone = dto.clone();
            clone.setPublic(true);
            clone.setJavadocComment(LotNo.TAG_PREFIXION + firstLineDto.getLotNo());
            clone.getAnnotations().clear();
            clone.addAnnotation(AnnotationConstant.DATA);
            clone.addAnnotation(AnnotationConstant.ACCESSORS);
            clone.addAnnotation(AnnotationConstant.FIELD_DEFAULTS_PRIVATE);
            Authors.ensureAuthorExist(clone, handlerTransformerConfig.getAuthor());
            builder.coid(clone);
            if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
                result.setParamType(calcType(dto));
                result.setReqDtoQualifier(pkg + "." + clone.getNameAsString());
            }
            if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
                result.setResultType(calcType(dto));
                result.setRespDtoQualifier(pkg + "." + clone.getNameAsString());
            }
            builders.add(builder);
            dtoQualifiers.add(pkg + "." + clone.getNameAsString());

            // 遍历到NestDto时，将父节点中的自身替换为Field
            if (dto.getParentNode().filter(parent -> parent instanceof ClassOrInterfaceDeclaration).isPresent()) {
                ClassOrInterfaceDeclaration parentCoid = (ClassOrInterfaceDeclaration) dto.getParentNode().get();
                FieldDeclaration field = new FieldDeclaration();
                if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
                    field.addAnnotation(AnnotationConstant.VALID);
                }
                this.moveAnnotationsFromDtoToField(dto, field);
                field.setPrivate(true).addVariable(new VariableDeclarator(StaticJavaParser.parseType(calcType(dto)),
                        standardizeNestDtoFieldName(dto)));
                parentCoid.replace(dto, field);
            }
        }
        for (JavabeanCuBuilder<JavabeanTypeEnum> builder : builders) {
            builder.importDeclarationsString(dtoQualifiers);
            CompilationUnit javabeanCu = builder.build();

            // Field的额外操作
            Set<String> importNames = Sets.newHashSet();
            for (FieldDeclaration field : builder.getJavabean().getFields()) {
                BeforeJavabeanCuBuildResult before = fieldHandle.beforeJavabeanCuBuild(field, builder.getContext());
                builder.getJavabean().replace(field, before.getField());
                importNames.addAll(before.getAppendImports());
            }
            importNames.forEach(importName -> Imports.ensureImported(javabeanCu, importName));

            Saves.add(javabeanCu);
            log.info("generate Javabean [{}].", builder.getJavabean().getNameAsString());
            result.getDtoQualifiers().add(builder.getJavabeanQualifier());
        }
        return result;
    }

    private String standardizeNestDtoFieldName(ClassOrInterfaceDeclaration dto) {
        boolean isCollectionOrPage =
                dto.getAnnotationByName("L").isPresent() || dto.getAnnotationByName("P").isPresent();
        String typeName = dto.getNameAsString();
        String fieldName = MoreStringUtils.lowerFirstLetter(typeName);
        fieldName = StringUtils.removeEndIgnoreCase(fieldName, "dto");
        if (isCollectionOrPage) {
            fieldName = English.plural(fieldName);
        }
        return fieldName;
    }

    private JavabeanTypeEnum estimateJavabeanType(ClassOrInterfaceDeclaration dto) {
        JavabeanTypeEnum javabeanType;
        if (dto.getNameAsString().equalsIgnoreCase("Req")) {
            javabeanType = JavabeanTypeEnum.REQ_DTO;
        } else if (dto.getNameAsString().equalsIgnoreCase("Resp")) {
            javabeanType = JavabeanTypeEnum.RESP_DTO;
        } else if (dto.findAncestor(ClassOrInterfaceDeclaration.class,
                ancestor -> ancestor.getNameAsString().equalsIgnoreCase("Req")).isPresent()) {
            javabeanType = JavabeanTypeEnum.NEST_DTO_IN_REQ;
        } else if (dto.findAncestor(ClassOrInterfaceDeclaration.class,
                ancestor -> ancestor.getNameAsString().equalsIgnoreCase("Resp")).isPresent()) {
            javabeanType = JavabeanTypeEnum.NEST_DTO_IN_RESP;
        } else {
            throw new RuntimeException("impossible unless bug.");
        }
        return javabeanType;
    }

    private void moveAnnotationsFromDtoToField(ClassOrInterfaceDeclaration dto, FieldDeclaration field) {
        for (AnnotationExpr annotation : dto.getAnnotations()) {
            if (!StringUtils.equalsAny(annotation.getNameAsString(), "L", "P")) {
                field.addAnnotation(annotation);
            }
        }
    }

    private String standardizeJavabeanName(FirstLineDto firstLineDto, ClassOrInterfaceDeclaration dto,
            JavabeanTypeEnum javabeanType) {
        String javaBeanName;
        if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
            javaBeanName = MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "ReqDto";
        } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
            javaBeanName = MoreStringUtils.upperFirstLetter(firstLineDto.getHandlerName()) + "RespDto";
        } else {
            String originName = dto.getNameAsString();
            if (!MoreStringUtils.endsWithIgnoreCase(originName, "dto")) {
                javaBeanName = MoreStringUtils.upperFirstLetter(originName) + "Dto";
            } else {
                javaBeanName = originName;
            }
        }
        return javaBeanName;
    }


    private String calcType(ClassOrInterfaceDeclaration dto) {
        if (dto.getAnnotationByName("L").isPresent()) {
            return "Collection<" + dto.getNameAsString() + ">";
        }
        if (dto.getAnnotationByName("P").isPresent()) {
            String[] split = handlerTransformerConfig.getPageTypeQualifier().split("\\.");
            return split[split.length - 1] + "<" + dto.getNameAsString() + ">";
        }
        return dto.getNameAsString();
    }

}