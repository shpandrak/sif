package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:50
 */
public class LongFieldDescriptor extends FieldDescriptor<Long> {

    public LongFieldDescriptor(String name) {
        super(name, FieldType.LONG);
    }

    @Override
    public Class<Long> getFieldClassType() {
        return Long.class;
    }

    @Override
    public Long fromString(String value) {
        if (value == null) {
            return null;
        } else
            return Long.valueOf(value);
    }
}


