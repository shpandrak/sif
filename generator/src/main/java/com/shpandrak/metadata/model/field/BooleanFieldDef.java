package com.shpandrak.metadata.model.field;

import com.shpandrak.common.model.FieldType;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/10/12
 * Time: 20:33
 */
public class BooleanFieldDef extends AbstractFieldMetadata{

    protected BooleanFieldDef() {
    }

    public BooleanFieldDef(String name) {
        super(name);
    }

    @Override
    public FieldType getType() {
        return FieldType.BOOLEAN;
    }
}
