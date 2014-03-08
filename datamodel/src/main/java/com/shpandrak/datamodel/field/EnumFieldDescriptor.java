package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 19:07
 */
public class EnumFieldDescriptor<T extends Enum> extends FieldDescriptor<T> {
    private Class<T> enumClass;
    private T[] values;

    public EnumFieldDescriptor(Class<T> enumClass, T[] values, String name) {
        super(name, FieldType.ENUM);
        this.enumClass = enumClass;
        this.values = values;
    }

    @Override
    public Class<T> getFieldClassType() {
        return enumClass;
    }

    @Override
    public T fromString(String value) {
        if (value == null) return null;
        return  ((T)T.<T>valueOf(getFieldClassType(), value));
    }

    public T[] getValues() {
        return values;
    }
}
