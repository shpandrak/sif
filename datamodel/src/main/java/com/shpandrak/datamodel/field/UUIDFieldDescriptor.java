package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:50
 */
public class UUIDFieldDescriptor extends FieldDescriptor<UUIDKey>{

    public UUIDFieldDescriptor(String name) {
        super(name, FieldType.KEY);
    }

    @Override
    public Class<UUIDKey> getFieldClassType() {
        return UUIDKey.class;
    }

    @Override
    public UUIDKey fromString(String value) {
        if (value == null) return null;
        return new UUIDKey(value);
    }

}
