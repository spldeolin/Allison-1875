package com.spldeolin.allison1875.querytransformer.dto;

import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
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

    DesignMetaDTO designMeta;

    ChainAnalysisDTO chainAnalysis;

    GenerateParamRetval generateParamRetval;

    GenerateReturnTypeRetval generateReturnTypeRetval;

    String mapperVarName;

}