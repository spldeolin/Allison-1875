package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeBodyRetval;
import com.spldeolin.allison1875.docanalyzer.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.service.JsonSchemaTransformerService;
import com.spldeolin.allison1875.docanalyzer.service.ResponseBodyService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 解析ResponseBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class ResponseBodyServiceImpl implements ResponseBodyService {

    @Inject
    private JsonSchemaTransformerService jsonSchemaTransformerService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public AnalyzeBodyRetval analyzeBody(JsonSchemaGenerator jsg, ClassOrInterfaceDeclaration mvcControllerCoid,
            MethodDeclaration mvcHandlerMd) {
        ResolvedType responseBodyType = getResponseBodyType(mvcControllerCoid, mvcHandlerMd);
        if (responseBodyType == null) {
            return new AnalyzeBodyRetval();
        }

        String responseBodyDescribe;
        if (responseBodyType.isPrimitive()) {
            responseBodyDescribe = ((ResolvedPrimitiveType) responseBodyType).getBoxTypeQName();
        } else {
            responseBodyDescribe = responseBodyType.describe();
        }

        JsonSchema jsonSchema;
        try {
            jsonSchema = JsonSchemaGenerateUtils.generateSchema(responseBodyDescribe, jsg);
        } catch (JsonSchemaException e) {
            log.error("fail to generateSchema, responseBodyDescribe={}", responseBodyDescribe);
            throw e;
        }
        jsonSchemaTransformerService.transformReferenceSchema(jsonSchema);

        return new AnalyzeBodyRetval().setDescribe(responseBodyDescribe).setJsonSchema(jsonSchema);
    }

    private ResolvedType getResponseBodyType(ClassOrInterfaceDeclaration mvcControllerCoid,
            MethodDeclaration mvcHandlerMd) {
        String name = MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandlerMd);

        if (isNotRestController(mvcControllerCoid) && isNotResponseBody(mvcHandlerMd)) {
            log.info("mvcHandler '{}' is neither a @ResponseBody nor in a @RestController", name);
            return null;
        }

        try {
            return this.getConcernedResponseBodyType(mvcHandlerMd);
        } catch (Exception e) {
            log.error("fail to get concerned ResponseBody Type, mvcHandler={}", name);
            throw e;
        }
    }

    private boolean isNotRestController(ClassOrInterfaceDeclaration coid) {
        String qualifier = annotationExprService.springRestController().getNameAsString();
        return !annotationExprService.isAnnotated(qualifier, coid);
    }

    private boolean isNotResponseBody(MethodDeclaration md) {
        String qualifier = annotationExprService.springResponseBody().getNameAsString();
        return !annotationExprService.isAnnotated(qualifier, md);
    }

    protected ResolvedType getConcernedResponseBodyType(MethodDeclaration mvcHandlerMd) {
        return mvcHandlerMd.getType().resolve();
    }

}
