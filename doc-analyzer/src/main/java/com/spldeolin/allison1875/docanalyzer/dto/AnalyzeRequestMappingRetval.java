package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-04
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeRequestMappingRetval {

    List<String> combinedUrls;

    List<RequestMethod> combinedVerbs;

}