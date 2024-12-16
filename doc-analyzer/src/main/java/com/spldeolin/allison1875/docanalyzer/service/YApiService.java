package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.service.impl.YApiServiceImpl;

/**
 * 将endpoints同步到YApi
 *
 * @author Deolin 2023-12-23
 */
@ImplementedBy(YApiServiceImpl.class)
public interface YApiService {

    void flushToYApi(List<EndpointDTO> endpoints);

}