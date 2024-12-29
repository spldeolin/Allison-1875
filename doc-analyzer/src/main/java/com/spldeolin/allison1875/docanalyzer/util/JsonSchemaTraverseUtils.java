package com.spldeolin.allison1875.docanalyzer.util;

import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import com.google.common.base.Preconditions;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-08-02
 */
@Slf4j
public class JsonSchemaTraverseUtils {

    private JsonSchemaTraverseUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public interface SchemaNodeCallback {

        void callback(String propertyName, JsonSchema jsonSchema, JsonSchema parentJsonSchema, int depth);

    }

    public static void traverse(JsonSchema jsonSchema, SchemaNodeCallback callback) {
        Preconditions.checkNotNull(jsonSchema);
        Preconditions.checkNotNull(callback);
        traverse("", jsonSchema, callback, 0);
    }

    private static void traverse(String propName, JsonSchema jsonSchema, SchemaNodeCallback callback, int depth) {
        if (jsonSchema.isObjectSchema()) {
            Map<String, JsonSchema> properties = jsonSchema.asObjectSchema().getProperties();
            properties.forEach((childPropName, childJsonSchema) -> {
                callback.callback(childPropName, childJsonSchema, jsonSchema, depth);
                int nextDepth = childJsonSchema.isObjectSchema() ? depth + 1 : depth;
                traverse(childPropName, childJsonSchema, callback, nextDepth);
            });
        }
        if (jsonSchema.isArraySchema()) {
            Items items = jsonSchema.asArraySchema().getItems();
            if (items == null) {
                return;
            }
            if (items.isSingleItems()) {
                JsonSchema elementJsonSchema = items.asSingleItems().getSchema();
                if (elementJsonSchema.isArraySchema()) {
                    throw new UnsupportedOperationException(
                            "出现了多维数组，doc-analyzer暂不支持解析 " + jsonSchema.getDescription());
                }
                callback.callback(propName, elementJsonSchema, jsonSchema, depth);
                int nextDepth = elementJsonSchema.isObjectSchema() ? depth + 1 : depth;
                traverse(propName, elementJsonSchema, callback, nextDepth);
            }
            if (items.isArrayItems()) {
                // Java没有tuple语法，所以这个情况不可能存在
                throw new Allison1875Exception("This is impossible because there is no tuple in Java");
            }
        }
    }

}