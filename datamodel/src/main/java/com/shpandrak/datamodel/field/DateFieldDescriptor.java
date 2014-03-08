package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

import java.util.Date;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:50
 */
public class DateFieldDescriptor extends FieldDescriptor<Date> {

    public DateFieldDescriptor(String name) {
        super(name, FieldType.DATE);
    }

    @Override
    public Class<Date> getFieldClassType() {
        return Date.class;
    }

    @Override
    public Date fromString(String value) {
        if (value == null) {
            return null;
        } else
            return new Date(Long.valueOf(value));
    }
}


