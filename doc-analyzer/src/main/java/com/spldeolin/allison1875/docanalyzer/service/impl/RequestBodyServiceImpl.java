package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeBodyRetval;
import com.spldeolin.allison1875.docanalyzer.service.JsonSchemaTransformerService;
import com.spldeolin.allison1875.docanalyzer.service.RequestBodyService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.MethodQualifierUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 内聚了 解析RequestBody的功能
 *
 * @author Deolin 2020-06-10
 */
@Singleton
@Slf4j
public class RequestBodyServiceImpl implements RequestBodyService {

    @Inject
    private JsonSchemaTransformerService jsonSchemaTransformerService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public AnalyzeBodyRetval analyzeBody(JsonSchemaGenerator jsg, MethodDeclaration mvcHandlerMd) {
        ResolvedType requestBodyType = getRequestBodyType(mvcHandlerMd);
        if (requestBodyType == null) {
            return new AnalyzeBodyRetval();
        }

        String requestBodyDescribe;
        if (requestBodyType.isPrimitive()) {
            requestBodyDescribe = ((ResolvedPrimitiveType) requestBodyType).getBoxTypeQName();
        } else {
            requestBodyDescribe = requestBodyType.describe();
        }

        JsonSchema jsonSchema;
        try {
            jsonSchema = JsonSchemaGenerateUtils.generateSchema(requestBodyDescribe, jsg);
        } catch (Exception e) {
            log.error("fail to generateSchema, requestBodyDescribe={}", requestBodyDescribe);
            throw e;
        }
        jsonSchemaTransformerService.transformReferenceSchema(jsonSchema);

        return new AnalyzeBodyRetval().setDescribe(requestBodyDescribe).setJsonSchema(jsonSchema);
    }

    private ResolvedType getRequestBodyType(MethodDeclaration mvcHandlerMd) {
        String name = MethodQualifierUtils.getTypeQualifierWithMethodName(mvcHandlerMd);

        List<Parameter> requestBodyParams = Lists.newArrayList();
        for (Parameter param : mvcHandlerMd.getParameters()) {
            for (AnnotationExpr anno : param.getAnnotations()) {
                if (isRequestBody(param)) {
                    requestBodyParams.add(param);
                    break;
                } else {
                    log.warn("mvcHanler '{}' has non-RequestBody param '{}', Allison 1875 is not supported for "
                            + "analyzing", name, param);
                }
            }
        }

        if (requestBodyParams.isEmpty()) {
            log.info("mvcHanler '{}' has no param", name);
            return null;
        }

        if (requestBodyParams.size() > 1) {
            log.warn("mvcHanler '{}' unexpectedly has more than one RequestBody param, ignoring anything other than "
                    + "the first one", name);
        }

        Parameter onlyOrFirst = requestBodyParams.get(0);
        try {
            return onlyOrFirst.getType().resolve();
        } catch (Exception e) {
            log.error("parameter '{}' of type cannot resolve", onlyOrFirst, e);
            return null;
        }
    }

    private boolean isRequestBody(Parameter parameter) {
        String qualifier = annotationExprService.springRequestBody().getNameAsString();
        return annotationExprService.isAnnotated(qualifier, parameter);
    }

}
