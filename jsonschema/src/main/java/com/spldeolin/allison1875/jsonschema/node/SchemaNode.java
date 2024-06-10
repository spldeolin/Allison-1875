package com.spldeolin.allison1875.jsonschema.node;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public abstract class SchemaNode {

    public abstract String getName();

    public abstract Optional<SchemaNode> getParent();

    public abstract String getPath();

//    public <T extends SchemaNode> List<T> findAll(Class<T> nodeType) {
//
//    }

    public boolean isStringSchema() {
        return false;
    }

    public StringSchemaNode asStringSchema() {
        throw new ClassCastException("JsonSchema is not a StringSchema");
    }

    public boolean isContainerSchema() {
        return false;
    }

    public boolean isObjectSchema() {
        return false;
    }

    public ObjectSchemaNode asObjectSchema() {
        throw new ClassCastException("JsonSchema is not a ObjectSchema");
    }

    public boolean isArraySchema() {
        return false;
    }

    public ArraySchemaNode asArraySchema() {
        throw new ClassCastException("JsonSchema is not a ObjectSchema");
    }

    public boolean isUnknownSchema() {
        return false;
    }

    public UnknownSchemaNode asUnknownSchema() {
        throw new ClassCastException("JsonSchema is not a UnknownSchema");
    }

    protected String initPropertyPath() {
        String propertyName = getName();
        if (this.isArraySchema()) {
            propertyName += "[]";
        }
        if (getParent().filter(SchemaNode::isArraySchema).isPresent()) {
            return getParent().get().getPath();
        }
        if (getParent().filter(parent -> StringUtils.isNotEmpty(parent.getName())).isPresent()) {
            return getParent().get().getPath() + "." + propertyName;
        }
        return propertyName;
    }

}
