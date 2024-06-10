package com.spldeolin.allison1875.jsonschema;

import java.util.Map.Entry;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.jsonschema.node.ArraySchemaNode;
import com.spldeolin.allison1875.jsonschema.node.ObjectSchemaNode;
import com.spldeolin.allison1875.jsonschema.node.SchemaNode;
import com.spldeolin.allison1875.jsonschema.node.StringSchemaNode;
import com.spldeolin.allison1875.jsonschema.node.UnknownSchemaNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSchemaNodeBuilder {

    public SchemaNode build(JsonSchema jsonSchema) {
        return buildRecursively(jsonSchema, "", null);
    }

    private SchemaNode buildRecursively(JsonSchema self, String propertyName, SchemaNode parent) {
        if (self.isObjectSchema()) {
            ObjectSchemaNode selfNode = new ObjectSchemaNode(propertyName, parent, self.getId(), Lists.newArrayList());
            for (Entry<String, JsonSchema> entry : self.asObjectSchema().getProperties().entrySet()) {
                String childPropertyName = entry.getKey();
                JsonSchema child = entry.getValue();
                SchemaNode childNode = this.buildRecursively(child, childPropertyName, selfNode);
                selfNode.listChildren().add(childNode);
            }
            return selfNode;

        } else if (self.isArraySchema()) {
            ArraySchemaNode selfNode = new ArraySchemaNode(propertyName, parent);
            Items element = self.asArraySchema().getItems();
            if (element == null) {
                log.warn("Allison1875 does not support parsing unparameterized arrays");
            } else if (element.isSingleItems()) {
                if (element.asSingleItems().getSchema().isArraySchema()) {
                    log.warn("Allison1875 does not support parsing multidimensional arrays");
                } else {
                    SchemaNode child = buildRecursively(element.asSingleItems().getSchema(), propertyName, selfNode);
                    selfNode.setChild(child);
                }
            } else if (element.isArrayItems()) {
                log.warn("Allison1875 does not support parsing multiple unparameterized arrays");
            } else {
                throw new RuntimeException("ArraySchema.Items MUST be a self or an array of jsonSchemas.");
            }
            return selfNode;

        } else if (self.isSimpleTypeSchema()) {
            if (self.isStringSchema()) {
                return new StringSchemaNode(propertyName, parent);
            } else {
                // TODO
                return new UnknownSchemaNode(propertyName, parent);
            }

        } else {
            log.warn("Allison1875 does not support parsing JsonSchema other than ContainerTypeSchema and "
                    + "SimpleTypeSchema");
            return new UnknownSchemaNode(propertyName, parent);
        }
    }

}
