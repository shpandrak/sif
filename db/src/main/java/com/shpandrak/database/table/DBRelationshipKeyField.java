package com.shpandrak.database.table;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/4/12
 * Time: 13:17
 */
public class DBRelationshipKeyField extends DBKeyField {
    private final DBKeyField relatedField;

    public DBRelationshipKeyField(FieldDescriptor<Key> fieldDescriptor, String persistentFieldName, DBKeyField relatedField) {
        super(fieldDescriptor, persistentFieldName);
        this.relatedField = relatedField;
    }

    public DBRelationshipKeyField(KeyFieldDescriptor<? extends Key> fieldDescriptor, DBKeyField relatedField) {
        super(fieldDescriptor);
        this.relatedField = relatedField;
    }

    public DBKeyField getRelatedField() {
        return relatedField;
    }
}
