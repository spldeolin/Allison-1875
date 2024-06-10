package com.spldeolin.allison1875.jsonschema.node;

import java.util.Optional;

public class ArraySchemaNode extends SchemaNode {

    private final String name;

    private final SchemaNode parent;

    private final String path;

    private SchemaNode child;

    public ArraySchemaNode(String propertyName, SchemaNode parent) {
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

    public void setChild(SchemaNode child) {
        this.child = child;
    }

    public Optional<SchemaNode> getChild() {
        return Optional.ofNullable(child);
    }

    @Override
    public boolean isContainerSchema() {
        return true;
    }

    public boolean isArraySchema() {
        return true;
    }

    @Override
    public ArraySchemaNode asArraySchema() {
        return this;
    }

}
