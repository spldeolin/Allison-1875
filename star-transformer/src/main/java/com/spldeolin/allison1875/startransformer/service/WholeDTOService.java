package com.spldeolin.allison1875.startransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.startransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.startransformer.service.impl.WholeDTOServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(WholeDTOServiceImpl.class)
public interface WholeDTOService {

    DataModelGeneration generateWholeDTO(ChainAnalysisDTO analysis);

}