package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-12-08
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueryByKeysDTO {

    PropertyDTO key;

    String methodName;

    String varsName;

}