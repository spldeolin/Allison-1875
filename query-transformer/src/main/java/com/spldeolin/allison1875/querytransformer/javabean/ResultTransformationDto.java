package com.spldeolin.allison1875.querytransformer.javabean;

import java.util.Map;
import com.github.javaparser.ast.type.Type;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-06-01
 */
@Data
@Accessors(chain = true)
public class ResultTransformationDto {

    private String oneImport;

    private Type resultType;

    private Map<String, String> propertyName2VarNames;

}