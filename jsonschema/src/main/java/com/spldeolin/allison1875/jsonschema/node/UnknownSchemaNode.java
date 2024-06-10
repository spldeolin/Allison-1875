package com.spldeolin.allison1875.jsonschema.node;

import java.util.Optional;

public class UnknownSchemaNode extends SchemaNode {

    private final String name;

    private final SchemaNode parent;

    private final String path;

    public UnknownSchemaNode(String propertyName, SchemaNode parent) {
        this.name = propertyName;
        this.parent = parent;
        this.path = initPropertyPath();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<SchemaNode> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public String getPath() {
        return path;
    }

}
