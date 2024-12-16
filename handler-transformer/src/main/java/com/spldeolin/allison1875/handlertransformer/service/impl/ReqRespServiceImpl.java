package com.spldeolin.allison1875.handlertransformer.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.enums.JavabeanTypeEnum;
import com.spldeolin.allison1875.handlertransformer.javabean.GenerateDTOJavabeansRetval;
import com.spldeolin.allison1875.handlertransformer.javabean.InitDecAnalysisDTO;
import com.spldeolin.allison1875.handlertransformer.service.FieldService;
import com.spldeolin.allison1875.handlertransformer.service.ReqRespService;
import com.spldeolin.allison1875.support.GetUrlQuery;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Slf4j
public class ReqRespServiceImpl implements ReqRespService {

    @Inject
    private CommonConfig commonConfig;
    
    @Inject
    private HandlerTransformerConfig config;

    @Inject
    private FieldService fieldService;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void validInitBody(BlockStmt initBody, InitDecAnalysisDTO initDecAnalysis) {
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 2) {
            throw new IllegalArgumentException(
                    "构造代码块下最多只能有2个类声明，分别用于代表ReqDTO和RespDTO。[" + initDecAnalysis + "] 当前："
                            + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                            .map(one -> one.getClassDeclaration().getNameAsString()).collect(Collectors.joining("、")));
        }
        if (CollectionUtils.isNotEmpty(initBody.findAll(LocalClassDeclarationStmt.class))) {
            for (LocalClassDeclarationStmt lcds : initBody.findAll(LocalClassDeclarationStmt.class)) {
                if (!StringUtils.equalsAnyIgnoreCase(lcds.getClassDeclaration().getNameAsString(), "Req", "Resp")) {
                    throw new IllegalArgumentException(
                            "构造代码块下类的命名只能是「Req」或者「Resp」。[" + initDecAnalysis + "] 当前："
                                    + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                                    .map(one -> one.getClassDeclaration().getNameAsString())
                                    .collect(Collectors.joining("、")));
                }
            }
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Req")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Req类。[" + initDecAnalysis + "]");
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getNameAsString().equals("Resp")).size()
                > 1) {
            throw new IllegalArgumentException("构造代码块下不能重复声明Resp类。[" + initDecAnalysis + "]");
        }
    }

    @Override
    public GenerateDTOJavabeansRetval generateDTOJavabeans(InitDecAnalysisDTO initDecAnalysis,
            List<ClassOrInterfaceDeclaration> dtos) {
        GenerateDTOJavabeansRetval result = new GenerateDTOJavabeansRetval();

        // 生成ReqDTO、RespDTO、NestDTO
        for (ClassOrInterfaceDeclaration dto : dtos) {
            JavabeanTypeEnum javabeanType = estimateJavabeanType(dto);
            String packageName = estimatePackageName(javabeanType);
            String javabeanName = standardizeJavabeanName(initDecAnalysis, dto, javabeanType);

            if (BooleanUtils.isNotTrue(result.getIsHttpGet())) {
                result.setIsHttpGet(javabeanType == JavabeanTypeEnum.REQ_PARAMS);
            }

            if (javabeanType == JavabeanTypeEnum.REQ_PARAMS) {
                for (FieldDeclaration fd : dto.getFields()) {
                    result.getReqParams().addAll(fd.getVariables());
                }
                continue;
            }

            JavabeanArg arg = new JavabeanArg();
            arg.setAstForest(AstForestContext.get());
            arg.setPackageName(packageName);
            arg.setClassName(javabeanName);
            arg.setDescription(concatDTODescription(initDecAnalysis));
            arg.setAuthor(commonConfig.getAuthor());
            arg.setIsJavabeanSerializable(commonConfig.getIsJavabeanSerializable());
            arg.setIsJavabeanCloneable(commonConfig.getIsJavabeanCloneable());
            arg.setMore4Javabean((tempCu, javabean) -> {
                for (FieldDeclaration field : dto.getFields()) {
                    fieldService.more4SpecialTypeField(field, javabeanType);
                }
                importExprService.copyImports(initDecAnalysis.getMvcControllerCu(), tempCu);
                javabean.setMembers(dto.getMembers());
            });
            arg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration javabeanGeneration = javabeanGeneratorService.generate(arg);
            result.getFlushes().add(FileFlush.build(javabeanGeneration.getCu()));

            dto.setName(javabeanGeneration.getJavabeanName());

            String javabeanQualifier = javabeanGeneration.getJavabeanQualifier();
            if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
                result.setReqBodyDTOType(calcType(dto, javabeanQualifier));
            }
            if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
                result.setRespBodyDTOType(calcType(dto, javabeanQualifier));
            }

            // 遍历到NestDTO时，将父节点中的自身替换为Field
            if (Lists.newArrayList(JavabeanTypeEnum.NEST_DTO_IN_REQ, JavabeanTypeEnum.NEST_DTO_IN_RESP)
                    .contains(javabeanType)) {
                ClassOrInterfaceDeclaration parentCoid = (ClassOrInterfaceDeclaration) dto.getParentNode()
                        .orElseThrow(() -> new Allison1875Exception("cannot find parent for" + dto.getName()));
                FieldDeclaration field = new FieldDeclaration();
                dto.getJavadoc().ifPresent(field::setJavadocComment);
                if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
                    field.addAnnotation(annotationExprService.javaxValid());
                }
                this.moveAnnotations(dto, field);
                field.addVariable(new VariableDeclarator(StaticJavaParser.parseType(calcType(dto, javabeanQualifier)),
                        standardizeNestDTOFieldName(dto)));
                parentCoid.replace(dto, field);
            }
        }

        return result;
    }

    private String estimatePackageName(JavabeanTypeEnum javabeanType) {
        String packageName;
        if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
            packageName = commonConfig.getReqDTOPackage();
        } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
            packageName = commonConfig.getRespDTOPackage();
        } else if (javabeanType == JavabeanTypeEnum.NEST_DTO_IN_REQ) {
            packageName = commonConfig.getReqDTOPackage();
        } else {
            packageName = commonConfig.getRespDTOPackage();
        }
        return packageName;
    }

    private String standardizeNestDTOFieldName(ClassOrInterfaceDeclaration dto) {
        boolean isCollectionOrPage =
                dto.getAnnotationByName("L").isPresent() || dto.getAnnotationByName("P").isPresent();
        String typeName = dto.getNameAsString();
        String fieldName = MoreStringUtils.toLowerCamel(typeName);
        fieldName = StringUtils.removeEndIgnoreCase(fieldName, "dto");
        if (isCollectionOrPage) {
            fieldName = English.plural(fieldName);
        }
        return fieldName;
    }

    private JavabeanTypeEnum estimateJavabeanType(ClassOrInterfaceDeclaration dto) {
        JavabeanTypeEnum javabeanType;
        if (dto.getNameAsString().equalsIgnoreCase("Req")) {
            if (dto.getAnnotationByName(GetUrlQuery.class.getSimpleName()).isPresent()) {
                javabeanType = JavabeanTypeEnum.REQ_PARAMS;
            } else {
                javabeanType = JavabeanTypeEnum.REQ_DTO;
            }
        } else if (dto.getNameAsString().equalsIgnoreCase("Resp")) {
            javabeanType = JavabeanTypeEnum.RESP_DTO;
        } else if (dto.findAncestor(ClassOrInterfaceDeclaration.class,
                ancestor -> ancestor.getNameAsString().equalsIgnoreCase("Req")).isPresent()) {
            javabeanType = JavabeanTypeEnum.NEST_DTO_IN_REQ;
        } else if (dto.findAncestor(ClassOrInterfaceDeclaration.class,
                ancestor -> ancestor.getNameAsString().equalsIgnoreCase("Resp")).isPresent()) {
            javabeanType = JavabeanTypeEnum.NEST_DTO_IN_RESP;
        } else {
            throw new Allison1875Exception("unknown Name [" + dto.getNameAsString() + "]");
        }
        return javabeanType;
    }

    private void moveAnnotations(ClassOrInterfaceDeclaration dto, FieldDeclaration field) {
        for (AnnotationExpr annotation : dto.getAnnotations()) {
            if (!StringUtils.equalsAny(annotation.getNameAsString(), "L", "P")) {
                field.addAnnotation(annotation);
            }
        }
    }

    private String standardizeJavabeanName(InitDecAnalysisDTO initDecAnalysis, ClassOrInterfaceDeclaration dto,
            JavabeanTypeEnum javabeanType) {
        String javaBeanName;
        if (javabeanType == JavabeanTypeEnum.REQ_DTO) {
            javaBeanName = MoreStringUtils.toUpperCamel(initDecAnalysis.getMvcHandlerMethodName()) + "ReqDTO";
        } else if (javabeanType == JavabeanTypeEnum.RESP_DTO) {
            javaBeanName = MoreStringUtils.toUpperCamel(initDecAnalysis.getMvcHandlerMethodName()) + "RespDTO";
        } else {
            String originName = dto.getNameAsString();
            if (!StringUtils.endsWithIgnoreCase(originName, "dto")) {
                javaBeanName = MoreStringUtils.toUpperCamel(originName) + "DTO";
            } else {
                javaBeanName = originName;
            }
        }
        return javaBeanName;
    }


    private String calcType(ClassOrInterfaceDeclaration dto, String javabeanQualifier) {
        if (dto.getAnnotationByName("L").isPresent()) {
            return "java.util.List<" + javabeanQualifier + ">";
        }
        if (dto.getAnnotationByName("P").isPresent()) {
            return String.format("%s<%s>", config.getPageTypeQualifier(), javabeanQualifier);
        }
        return javabeanQualifier;
    }

    private String concatDTODescription(InitDecAnalysisDTO initDecAnalysis) {
        String result = "";
        if (commonConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION
                    + initDecAnalysis.getLotNo();
        }
        return result;
    }

}