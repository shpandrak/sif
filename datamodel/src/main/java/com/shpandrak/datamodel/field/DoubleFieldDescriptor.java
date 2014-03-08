package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:50
 */
public class DoubleFieldDescriptor extends FieldDescriptor<Double> {

    public DoubleFieldDescriptor(String name) {
        super(name, FieldType.DOUBLE);
    }

    @Override
    public Class<Double> getFieldClassType() {
        return Double.class;
    }

    @Override
    public Double fromString(String value) {
        if (value == null) {
            return null;
        } else
            return Double.valueOf(value);
    }
}


