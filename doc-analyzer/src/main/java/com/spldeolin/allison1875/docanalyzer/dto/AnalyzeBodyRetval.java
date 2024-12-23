package com.spldeolin.allison1875.docanalyzer.dto;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-12-13
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeBodyRetval {

    String describe;

    JsonSchema jsonSchema;

}