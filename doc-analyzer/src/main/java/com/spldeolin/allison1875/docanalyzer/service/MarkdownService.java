package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.MarkdownServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(MarkdownServiceImpl.class)
public interface MarkdownService {

    void flushToMarkdown(List<EndpointDto> endpoints);

}
