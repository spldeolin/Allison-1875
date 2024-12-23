package com.spldeolin.allison1875.common.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.GenerateMvcHandlerArgs;
import com.spldeolin.allison1875.common.dto.GenerateMvcHandlerRetval;
import com.spldeolin.allison1875.common.service.impl.MvcHandlerGeneratorServiceImpl;

/**
 * @author Deolin 2024-02-17
 */
@ImplementedBy(MvcHandlerGeneratorServiceImpl.class)
public interface MvcHandlerGeneratorService {

    GenerateMvcHandlerRetval generateMvcHandler(GenerateMvcHandlerArgs args);

}