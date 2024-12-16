package com.spldeolin.allison1875.common.service.impl;

import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.common.javabean.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.MvcHandlerGeneratorService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-17
 */
@Singleton
@Slf4j
public class MvcHandlerGeneratorServiceImpl implements MvcHandlerGeneratorService {

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public GenerateMvcHandlerRetval generateMvcHandler(GenerateMvcHandlerArgs args) {
        MethodDeclaration mvcHandler = new MethodDeclaration();

        if (StringUtils.isNotEmpty(args.getDescription())) {
            mvcHandler.setJavadocComment(args.getDescription());
        }

        String requestMapping;
        if (args.getIsHttpGet()) {
            requestMapping = "@org.springframework.web.bind.annotation.GetMapping(\"%s\")";
        } else {
            requestMapping = "@org.springframework.web.bind.annotation.PostMapping(\"%s\")";
        }

        mvcHandler.addAnnotation(
                StaticJavaParser.parseAnnotation(String.format(requestMapping, args.getMvcHandlerUrl())));

        mvcHandler.setPublic(true);

        String serviceResultType = args.getRespBodyDTOType();
        if (serviceResultType != null) {
            mvcHandler.setType(serviceResultType);
        } else {
            mvcHandler.setType(new VoidType());
        }

        String methodName = MoreStringUtils.toLowerCamel(args.getMvcHandlerUrl());
        if (args.getMvcController() != null) {
            methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, args.getMvcController());
        }
        mvcHandler.setName(methodName);

        String serviceParamType = args.getReqBodyDTOType();
        if (serviceParamType != null) {
            Parameter param = new Parameter();
            param.addAnnotation(annotationExprService.springRequestBody());
            param.addAnnotation(annotationExprService.javaxValid());
            param.setType(serviceParamType);
            param.setName("req");
            mvcHandler.addParameter(param);
        }
        if (!args.getReqParams().isEmpty()) {
            Javadoc javadoc = mvcHandler.getJavadoc().orElse(new JavadocComment().parse());
            for (VariableDeclarator vd : args.getReqParams()) {
                Optional<FieldDeclaration> fdOpt = vd.getParentNode().filter(p -> p instanceof FieldDeclaration)
                        .map(p -> (FieldDeclaration) p);
                Parameter param = new Parameter(vd.getType(), vd.getName());

                // @RequestParam
                NormalAnnotationExpr anno = annotationExprService.springRequestParamWithProperty();
                // @RequestParam.defaultValue
                vd.getInitializer().filter(Expression::isLiteralExpr).map(Expression::asLiteralExpr).ifPresent(init -> {
                    if (init.isLiteralStringValueExpr()) {
                        anno.addPair("defaultValue", new StringLiteralExpr(init.asLiteralStringValueExpr().getValue()));
                    }
                    if (init.isBooleanLiteralExpr()) {
                        anno.addPair("defaultValue",
                                new StringLiteralExpr(String.valueOf(init.asBooleanLiteralExpr().getValue())));
                    }
                });
                // @RequestParam.required
                fdOpt.ifPresent(fd -> {
                    if (isNoneAnnotated(fd, NotNull.class, NotEmpty.class,
                            org.hibernate.validator.constraints.NotEmpty.class, NotBlank.class,
                            org.hibernate.validator.constraints.NotBlank.class)) {
                        anno.addPair("required", new BooleanLiteralExpr(false));
                    }
                });
                // using @RequestParam instead of @RequestParam() if nessary
                param.addAnnotation(
                        anno.getPairs().isEmpty() ? annotationExprService.springRequestParamWithoutProperty() : anno);

                // @DateTimeFormat
                if (Lists.newArrayList("Date", "LocalDate", "LocalTime", "LocalDateTime")
                        .contains(vd.getTypeAsString())) {
                    param.addAnnotation(annotationExprService.springDateTimeFormat());
                }

                mvcHandler.addParameter(param);

                // Javadoc @param标签
                fdOpt.ifPresent(fd -> fd.getJavadoc().ifPresent(
                        jd -> javadoc.addBlockTag(Type.PARAM.name().toLowerCase(),
                                vd.getNameAsString() + " " + JavadocUtils.getComment(fd))));
            }
            mvcHandler.setJavadocComment(javadoc);
        }

        BlockStmt body = new BlockStmt();
        String returnOrNot = serviceResultType != null ? "return" : "";
        String serviceVarName = args.getInjectedServiceVarName();
        String serviceMethodName = args.getServiceMethodName();
        StringBuilder argNames = new StringBuilder(serviceParamType != null ? "req" : "");
        for (VariableDeclarator requestParam : args.getReqParams()) {
            if (argNames.length() > 0) {
                argNames.append(",");
            }
            argNames.append(requestParam.getName());
        }

        String statement = String.format("%s %s.%s(%s);", returnOrNot, serviceVarName, serviceMethodName, argNames);
        body.addStatement(StaticJavaParser.parseStatement(statement));

        mvcHandler.setBody(body);
        return new GenerateMvcHandlerRetval().setMvcHandler(mvcHandler);
    }

    private boolean isNoneAnnotated(NodeWithAnnotations<?> node, Class<? extends Annotation>... classes) {
        for (Class<? extends Annotation> clazz : classes) {
            if (node.getAnnotationByName(clazz.getSimpleName()).isPresent()) {
                return false;
            }
        }
        return true;
    }

}