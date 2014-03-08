package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:50
 */
public class BooleanFieldDescriptor extends FieldDescriptor<Boolean>{

    public BooleanFieldDescriptor(String name) {
        super(name, FieldType.BOOLEAN);
    }

    @Override
    public Class<Boolean> getFieldClassType() {
        return Boolean.class;
    }

    @Override
    public Boolean fromString(String value) {
        if (value == null) return null;
        return Boolean.valueOf(value);
    }
}
