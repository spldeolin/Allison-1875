package com.spldeolin.allison1875.querytransformer.enums;

import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2021-07-06
 */
@Getter
@AllArgsConstructor
public enum JoinTypeEnum {

    leftJoin,

    rightJoin,

    innerJoin,

    outerJoin,

    ;

    public static JoinTypeEnum of(String value) {
        switch (value) {
            case "left":
                return leftJoin;
            case "right":
                return rightJoin;
            case "inner":
                return innerJoin;
            case "outer":
                return outerJoin;
            default:
                throw new IllegalChainException("Unknown join type: " + value);
        }
    }

}