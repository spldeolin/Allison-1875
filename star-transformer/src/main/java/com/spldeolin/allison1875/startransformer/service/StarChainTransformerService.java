package com.spldeolin.allison1875.startransformer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.startransformer.dto.TransformStarChainArgs;
import com.spldeolin.allison1875.startransformer.service.impl.StarChainTransformerServiceImpl;

/**
 * @author Deolin 2023-12-28
 */
@ImplementedBy(StarChainTransformerServiceImpl.class)
public interface StarChainTransformerService {

    void transformStarChain(TransformStarChainArgs args);

}