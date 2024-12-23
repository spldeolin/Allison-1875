package com.spldeolin.allison1875.docanalyzer.dto;

import java.util.List;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-06-01
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointDTO {

    String cat;

    String handlerSimpleName;

    List<String> descriptionLines;

    Boolean isDeprecated;

    String author;

    String sourceCode;

    List<String> urls;

    String httpMethod;

    String requestBodyDescribe;

    JsonSchema requestBodyJsonSchema;

    String responseBodyDescribe;

    JsonSchema responseBodyJsonSchema;

    public EndpointDTO copy() {
        EndpointDTO result = new EndpointDTO();
        result.setCat(cat);
        result.setHandlerSimpleName(handlerSimpleName);
        result.setDescriptionLines(descriptionLines);
        result.setIsDeprecated(isDeprecated);
        result.setAuthor(author);
        result.setSourceCode(sourceCode);
        result.setUrls(urls);
        result.setHttpMethod(httpMethod);
        result.setRequestBodyDescribe(requestBodyDescribe);
        result.setRequestBodyJsonSchema(requestBodyJsonSchema);
        result.setResponseBodyDescribe(responseBodyDescribe);
        result.setResponseBodyJsonSchema(responseBodyJsonSchema);
        return result;
    }

}