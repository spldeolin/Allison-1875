package com.spldeolin.allison1875.querytransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.javabean.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.service.impl.MethodGeneratorServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(MethodGeneratorServiceImpl.class)
public interface MethodGeneratorService {

    GenerateParamRetval generateParam(ChainAnalysisDTO chainAnalysis);

    GenerateReturnTypeRetval generateReturnType(ChainAnalysisDTO chainAnalysis);

}