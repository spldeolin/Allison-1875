package com.spldeolin.allison1875.querytransformer.dto;

import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-11-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinedPropertyDTO implements VariableProperty {

    PropertyDTO property;

    String varName;

}