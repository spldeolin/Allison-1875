package com.spldeolin.allison1875.docanalyzer.service;

import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeBodyRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.RequestBodyServiceImpl;

/**
 * 内聚了 解析RequestBody的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(RequestBodyServiceImpl.class)
public interface RequestBodyService {

    AnalyzeBodyRetval analyzeBody(JsonSchemaGenerator jsg, MethodDeclaration mvcHandlerMd);

}
