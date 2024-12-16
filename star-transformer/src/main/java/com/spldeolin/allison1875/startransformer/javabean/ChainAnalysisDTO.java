package com.spldeolin.allison1875.startransformer.javabean;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.expr.Expression;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-02
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChainAnalysisDTO {

    String cftEntityQualifier;

    String cftEntityName;

    String cftDesignName;

    String cftDesignQualifier;

    @JsonIgnore
    Expression cftSecondArgument;

    List<PhraseDTO> phrases;

    String wholeDTOName;

    String lotNo;

}