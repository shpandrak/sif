package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:45
 */
public abstract class FieldDescriptor<T> {
    private final String name;
    private final FieldType fieldType;
    private ShpandrakObjectDescriptor objectDescriptor;
    private final boolean keyField;

    protected FieldDescriptor(String name, FieldType fieldType, boolean keyField) {
        this.name = name;
        this.fieldType = fieldType;
        this.keyField = keyField;
    }

    protected FieldDescriptor(String name, FieldType fieldType) {
        this(name, fieldType, false);
    }

    public void setObjectDescriptor(ShpandrakObjectDescriptor objectDescriptor) {
        this.objectDescriptor = objectDescriptor;
    }

    public abstract Class<T> getFieldClassType();

    public String getName() {
        return name;
    }

    public ShpandrakObjectDescriptor getObjectDescriptor() {
        return objectDescriptor;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public boolean isKeyField() {
        return keyField;
    }

    public abstract T fromString(String value);

}
