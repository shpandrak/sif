package com.shpandrak.datamodel.field;


import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/31/13
 * Time: 14:34
 */
public abstract class KeyFieldDescriptor<T extends Key> extends FieldDescriptor<T> {
    protected KeyFieldDescriptor(String name) {
        super(name, FieldType.KEY);
    }

    protected KeyFieldDescriptor(String name, boolean keyField) {
        super(name, FieldType.KEY, keyField);
    }
}
