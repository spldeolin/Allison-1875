package com.spldeolin.allison1875.docanalyzer.service;

import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.Table;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeFieldVarsRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.JsgBuilderServiceImpl;

/**
 * 内聚了 解析得到所有枚举、属性信息 和 生成自定义JsonSchemaGenerator对象的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(JsgBuilderServiceImpl.class)
public interface JsgBuilderService {

    JsonSchemaGenerator buildJsg(Table<String, String, AnalyzeFieldVarsRetval> analyzeFieldVarsRetvals,
            boolean forReqOrResp);

}
