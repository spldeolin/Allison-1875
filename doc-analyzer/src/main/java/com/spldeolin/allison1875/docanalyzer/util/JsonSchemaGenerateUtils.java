package com.spldeolin.allison1875.docanalyzer.util;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.docanalyzer.exception.DotAbsentInStringException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-03-01
 */
@Slf4j
public class JsonSchemaGenerateUtils {

    private JsonSchemaGenerateUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static JsonSchema generateSchema(String describe, JsonSchemaGenerator jsg) {
        TypeFactory tf = TypeFactory.defaultInstance();

        try {
            return jsg.generateSchema(tf.constructFromCanonical(describe));
        } catch (Throwable e) {
            try {
                // 考虑describe可能是内部类，所以递归地将describe的最后一个.替换成$并重新尝试generateSchema
                return generateSchemaRecursively(describe, tf, jsg);
            } catch (DotAbsentInStringException dotAbsent) {
                // 即便递归到describe中没有.可替换了，依然generateSchema失败，递归尝试失败，说明describe本身就无法处理（无法处理的原因见方法Javadoc）
                log.warn("Cannot generate the json schema, qualifierForClassLoader={}, reason={}", describe,
                        e.getMessage());
                throw new Allison1875Exception(e);
            }
        }
    }

    private static JsonSchema generateSchemaRecursively(String innerClassMightDescribe, TypeFactory tf,
            JsonSchemaGenerator jsg) throws DotAbsentInStringException {
        try {
            return jsg.generateSchema(tf.constructFromCanonical(innerClassMightDescribe));
        } catch (Throwable e) {
            innerClassMightDescribe = tryReplaceLastDotToDollar(innerClassMightDescribe);
            return generateSchemaRecursively(innerClassMightDescribe, tf, jsg);
        }
    }

    private static String tryReplaceLastDotToDollar(String innerClassMightDescribe) throws DotAbsentInStringException {
        int lastDotIndex = innerClassMightDescribe.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new DotAbsentInStringException();
        }
        return innerClassMightDescribe.substring(0, lastDotIndex) + '$' + innerClassMightDescribe.substring(
                lastDotIndex + 1);
    }

}
