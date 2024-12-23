package com.spldeolin.allison1875.docanalyzer.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.docanalyzer.enums.ValidatorTypeEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-04-25
 */
@JsonInclude(Include.NON_NULL)
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeValidRetval {

    /**
     * @see ValidatorTypeEnum
     */
    String validatorType;

    String note;

}
