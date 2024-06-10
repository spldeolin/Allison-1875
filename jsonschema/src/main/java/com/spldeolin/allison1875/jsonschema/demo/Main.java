package com.spldeolin.allison1875.jsonschema.demo;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.jsonschema.JsonSchemaNodeBuilder;
import com.spldeolin.allison1875.jsonschema.node.SchemaNode;

public class Main {

    public static void main(String[] args) {
        JsonSchema jsonSchema = JsonSchemaGenerateUtils.generateSchema(
                "com.spldeolin.allison1875.docanalyzer.jsonschema.demo.AA",
                new JsonSchemaGenerator(JsonUtils.createObjectMapper()));

        SchemaNode build = new JsonSchemaNodeBuilder().build(jsonSchema);

        System.out.println(build);
    }

}
