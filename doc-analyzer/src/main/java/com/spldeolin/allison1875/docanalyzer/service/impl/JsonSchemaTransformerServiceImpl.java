package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDTO;
import com.spldeolin.allison1875.docanalyzer.service.JsonSchemaTransformerService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;

/**
 * @author Deolin 2020-09-12
 */
@Singleton
public class JsonSchemaTransformerServiceImpl implements JsonSchemaTransformerService {

    @Override
    public void transformReferenceSchema(JsonSchema rootJsonSchema) {
        Map<String, String> pathsEachId = Maps.newHashMap();
        Map<JsonSchema, String> paths = Maps.newLinkedHashMap();
        if (rootJsonSchema.isObjectSchema()) {
            pathsEachId.put(rootJsonSchema.getId(), "根节点");
        }

        // 处理ReferenceSchema
        JsonSchemaTraverseUtils.traverse(rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            JsonPropertyDescriptionValueDTO jpdv = JsonPropertyDescriptionValueDTO.deserialize(
                    jsonSchema.getDescription());
            String path = paths.get(parentJsonSchema);
            if (path == null) {
                path = "";
            } else {
                if (parentJsonSchema.isObjectSchema()) {
                    path += ".";
                }
            }
            if (parentJsonSchema.isArraySchema()) {
                if (jsonSchema.isObjectSchema() || jsonSchema.isArraySchema()) {
                    propertyName = "";
                }
            }
            path = path + propertyName;
            if (jsonSchema.isArraySchema()) {
                path = path + "[]";
            }

            paths.put(jsonSchema, path);
            if (jsonSchema.getId() != null) {
                pathsEachId.put(jsonSchema.getId(), path);
            }

            if (jsonSchema instanceof ReferenceSchema) {
                String referencePath = pathsEachId.get(jsonSchema.get$ref());
                if (rootJsonSchema.isArraySchema()) {
                    referencePath = "根节点[]" + referencePath;
                }
                if (jpdv != null) {
                    jpdv.setReferencePath(referencePath);
                }
                if (parentJsonSchema.isArraySchema()) {
                    JsonPropertyDescriptionValueDTO parentJpdv = JsonPropertyDescriptionValueDTO.deserialize(
                            parentJsonSchema.getDescription());
                    if (parentJpdv == null) {
                        parentJpdv = new JsonPropertyDescriptionValueDTO();
                    }
                    parentJpdv.setReferencePath(referencePath);
                    parentJsonSchema.setDescription(parentJpdv.serialize());
                }
            }

            if (jpdv != null) {
                jsonSchema.setDescription(jpdv.serialize());
            }
        });
    }

}