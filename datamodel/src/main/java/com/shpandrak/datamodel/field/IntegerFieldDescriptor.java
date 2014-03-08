package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:50
 */
public class IntegerFieldDescriptor extends FieldDescriptor<Integer> {

    public IntegerFieldDescriptor(String name) {
        super(name, FieldType.INTEGER);
    }

    @Override
    public Class<Integer> getFieldClassType() {
        return Integer.class;
    }

    @Override
    public Integer fromString(String value) {
        if (value == null) {
            return null;
        } else
            return Integer.valueOf(value);
    }
}


