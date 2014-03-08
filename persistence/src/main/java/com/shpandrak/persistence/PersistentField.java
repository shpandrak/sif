package com.shpandrak.persistence;

import com.shpandrak.datamodel.field.FieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/24/12
 * Time: 09:57
 */
public abstract class PersistentField<T> {
    private final FieldDescriptor<T> fieldDescriptor;
    private final String persistentFieldName;

    public abstract Object prepareForPersisting(T value);

    protected PersistentField(FieldDescriptor<T> fieldDescriptor) {
        this(fieldDescriptor, fieldDescriptor.getName());
    }
    protected PersistentField(FieldDescriptor<T> fieldDescriptor, String persistentFieldName) {
        this.fieldDescriptor = fieldDescriptor;
        this.persistentFieldName = persistentFieldName;
    }

    public FieldDescriptor<T> getFieldDescriptor() {
        return fieldDescriptor;
    }

    public String getPersistentFieldName(){
        return persistentFieldName;
    }
}
