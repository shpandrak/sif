package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;
import sun.reflect.generics.tree.FieldTypeSignature;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/20/12
 * Time: 15:07
 */
public abstract class FieldInstance<T> {
    private final FieldDescriptor<T> descriptor;
    private T value;

    protected FieldInstance(FieldDescriptor<T> descriptor) {
        this.descriptor = descriptor;
    }

    public T getValue() {
        return value;
    }

    public String stringValue(){
        T value = this.value;
        if (value == null){
            return "";
        }else {
            return value.toString();
        }
    }

    public void setValue(T value) {
        this.value = value;
    }

    public FieldDescriptor<T> getDescriptor() {
        return descriptor;
    }

    public String getName(){
        return getDescriptor().getName();
    }

    public void fromString(String value){
        setValue(descriptor.fromString(value));
    }

    public FieldType getFieldType() {
        return descriptor.getFieldType();
    }
}
