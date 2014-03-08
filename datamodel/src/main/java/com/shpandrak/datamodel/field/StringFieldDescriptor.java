package com.shpandrak.datamodel.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 17:50
 */
public class StringFieldDescriptor extends FieldDescriptor<String>{

    public StringFieldDescriptor(String name, boolean keyField) {
        super(name, FieldType.STRING, keyField);
    }

    public StringFieldDescriptor(String name) {
        super(name, FieldType.STRING);
    }

    @Override
    public Class<String> getFieldClassType() {
        return String.class;
    }

    @Override
    public String fromString(String value) {
        return value;
    }
}
