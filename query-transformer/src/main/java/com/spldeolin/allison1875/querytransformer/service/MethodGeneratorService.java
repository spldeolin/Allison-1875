package com.spldeolin.allison1875.querytransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.service.impl.MethodGeneratorServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(MethodGeneratorServiceImpl.class)
public interface MethodGeneratorService {

    GenerateParamRetval generateParam(ChainAnalysisDTO chainAnalysis);

    GenerateReturnTypeRetval generateReturnType(ChainAnalysisDTO chainAnalysis);

}