package com.shpandrak.datamodel.field;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/20/12
 * Time: 15:11
 */
public class EnumField<T extends Enum> extends FieldInstance<T> {
    public EnumField(EnumFieldDescriptor<T> descriptor) {
        super(descriptor);
    }
}
