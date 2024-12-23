package com.spldeolin.allison1875.querytransformer.dto;

import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;

/**
 * @author Deolin 2024-11-23
 */
public interface CompareableBinary extends Binary {

    ComparisonOperatorEnum getComparisonOperator();

}
