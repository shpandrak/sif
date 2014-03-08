package com.shpandrak.metadata.model.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 5/11/13
 * Time: 14:16
 */
public class DoubleFieldDef extends AbstractFieldMetadata {
    protected DoubleFieldDef() {
    }

    public DoubleFieldDef(String name) {
        super(name);
    }

    @Override
    public FieldType getType() {
        return FieldType.DOUBLE;
    }
}
