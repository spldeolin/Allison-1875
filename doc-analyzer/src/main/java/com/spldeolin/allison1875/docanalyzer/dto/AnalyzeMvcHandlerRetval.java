package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-12
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeMvcHandlerRetval {

    String cat;

    List<String> descriptionLines;

    Boolean isDeprecated;

    String author;

    String sourceCode;

}