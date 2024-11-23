package com.spldeolin.allison1875.querytransformer.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2020-10-06
 */
@AllArgsConstructor
@Getter
public enum OrderSequenceEnum {

    DESC("desc"),

    ASC("asc"),

    ;

    private final String value;

    public static boolean isValid(String value) {
        return Arrays.stream(OrderSequenceEnum.values()).anyMatch(one -> one.getValue().equals(value));
    }

    public static OrderSequenceEnum of(String value) {
        return Arrays.stream(OrderSequenceEnum.values()).filter(one -> one.getValue().equals(value)).findAny()
                .orElse(null);
    }

}