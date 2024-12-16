package com.spldeolin.allison1875.persistencegenerator.facade.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2021-06-14
 */
public interface KeywordConstant {

    String META_FIELD_NAME = "meta";

    String WHERE_EVEN_NULL_METHOD_NAME = "whereEvenNull";

    String WHERE_METHOD_NAME = "where";

    @AllArgsConstructor
    @Getter
    enum ChainInitialMethod {

        SELECT("select", "query"),

        UPDATE("update", "update"),

        DELETE("delete", "delete");

        private final String code;

        private final String methodName;

    }

}