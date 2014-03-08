package com.shpandrak.metadata.model.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/17/12
 * Time: 08:24
 */
public class DateFieldDef extends AbstractFieldMetadata {
    protected DateFieldDef() {
    }

    public DateFieldDef(String name) {
        super(name);
    }

    @Override
    public FieldType getType() {
        return FieldType.DATE;
    }
}
