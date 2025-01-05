package com.spldeolin.allison1875.querytransformer.dto;

import java.util.List;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-06-01
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateParamRetval {

    final List<Parameter> parameters = Lists.newArrayList();

    Boolean isParamDTO;

    FileFlush paramDTOFlush;

}