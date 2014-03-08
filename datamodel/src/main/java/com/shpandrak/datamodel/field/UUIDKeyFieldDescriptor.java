package com.shpandrak.datamodel.field;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/31/13
 * Time: 14:41
 */
public class UUIDKeyFieldDescriptor extends KeyFieldDescriptor<UUIDKey> {
    public UUIDKeyFieldDescriptor(String name) {
        super(name);
    }

    @Override
    public Class<UUIDKey> getFieldClassType() {
        return UUIDKey.class;
    }

    @Override
    public UUIDKey fromString(String value) {
        return UUIDKey.fromStringValue(value);
    }
}
