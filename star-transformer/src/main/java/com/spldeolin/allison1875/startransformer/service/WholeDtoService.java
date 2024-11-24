package com.spldeolin.allison1875.startransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.impl.WholeDtoServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(WholeDtoServiceImpl.class)
public interface WholeDtoService {

    JavabeanGeneration generateWholeDto(ChainAnalysisDto analysis);

}