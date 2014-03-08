package com.shpandrak.datamodel.field;

import java.util.Date;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/20/12
 * Time: 15:10
 */
public class DateField extends FieldInstance<Date> {

    public DateField(FieldDescriptor<Date> descriptor) {
        super(descriptor);
    }

    @Override
    public String stringValue() {
        Date value = getValue();
        if (value == null){
            return null;
        }
        return String.valueOf(value.getTime());
    }
}
