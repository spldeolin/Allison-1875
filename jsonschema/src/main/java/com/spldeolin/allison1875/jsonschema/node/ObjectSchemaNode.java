package com.spldeolin.allison1875.jsonschema.node;

import java.util.List;
import java.util.Optional;

public class ObjectSchemaNode extends SchemaNode {

    private final String name;

    private final SchemaNode parent;

    private final String propertyPath;

    private final String urn;

    private final List<SchemaNode> children;


    public ObjectSchemaNode(String propertyName, SchemaNode parent, String urn, List<SchemaNode> children) {
        this.name = propertyName;
        this.parent = parent;
        this.urn = urn;
        this.children = children;
        this.propertyPath = initPropertyPath();
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
        return propertyPath;
    }

    @Override
    public boolean isContainerSchema() {
        return true;
    }

    @Override
    public boolean isObjectSchema() {
        return true;
    }

    @Override
    public ObjectSchemaNode asObjectSchema() {
        return this;
    }

    public String getUrn() {
        return urn;
    }

    public List<SchemaNode> listChildren() {
        return children;
    }

}
