package com.spldeolin.allison1875.startransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDTO;
import com.spldeolin.allison1875.startransformer.service.impl.WholeDTOServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(WholeDTOServiceImpl.class)
public interface WholeDTOService {

    JavabeanGeneration generateWholeDTO(ChainAnalysisDTO analysis);

}