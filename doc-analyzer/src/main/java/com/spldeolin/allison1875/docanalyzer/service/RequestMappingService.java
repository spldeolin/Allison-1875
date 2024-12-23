package com.spldeolin.allison1875.docanalyzer.service;

import java.lang.reflect.Method;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeRequestMappingRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.RequestMappingServiceImpl;

/**
 * 内聚了 对请求URL和请求动词解析的功能
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(RequestMappingServiceImpl.class)
public interface RequestMappingService {

    AnalyzeRequestMappingRetval analyzeRequestMapping(Class<?> controllerClass, Method reflectionMethod);

}
