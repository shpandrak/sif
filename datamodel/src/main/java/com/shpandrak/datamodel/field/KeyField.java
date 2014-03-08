package com.shpandrak.datamodel.field;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/31/13
 * Time: 14:46
 */
public class KeyField extends FieldInstance<Key> {
    public  KeyField(FieldDescriptor<? extends Key> descriptor) {
        super((FieldDescriptor<Key>) descriptor);
    }
}
