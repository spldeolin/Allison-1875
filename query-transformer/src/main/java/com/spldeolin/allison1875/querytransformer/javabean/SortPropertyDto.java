package com.spldeolin.allison1875.querytransformer.javabean;

import com.spldeolin.allison1875.querytransformer.enums.OrderSequenceEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-11-23
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SortPropertyDto {

    String propertyName;

    OrderSequenceEnum orderSequence;

}