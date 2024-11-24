package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.MvcHandlerDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.MvcHandlerDetectorServiceImpl;

/**
 * 从astForest发现mvcHandler
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MvcHandlerDetectorServiceImpl.class)
public interface MvcHandlerDetectorService {

    List<MvcHandlerDto> detectMvcHandler();

}
