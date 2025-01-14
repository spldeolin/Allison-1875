package com.spldeolin.allison1875.querytransformer.javabean;

import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-02-13
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReplaceDesignArgs {

    DesignMetaDto designMeta;

    ChainAnalysisDto chainAnalysis;

    GenerateParamRetval generateParamRetval;

    GenerateReturnTypeRetval generateReturnTypeRetval;

    String mapperVarName;

}