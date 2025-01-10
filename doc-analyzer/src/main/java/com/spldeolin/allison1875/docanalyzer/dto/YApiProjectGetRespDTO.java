package com.spldeolin.allison1875.docanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-02
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YApiProjectGetRespDTO {

    @JsonProperty("_id")
    Long id;

    String name;

    // and more...

}