package com.shpandrak.database.table;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/21/12
 * Time: 12:09
 */
public class DBEmbeddedRelationshipKeyField extends DBRelationshipKeyField {
    private EntityRelationshipDefinition relationshipDefinition;

    public DBEmbeddedRelationshipKeyField(KeyFieldDescriptor<? extends Key> fieldDescriptor, DBKeyField relatedField, EntityRelationshipDefinition relationshipDefinition) {
        super(fieldDescriptor, relatedField);
        this.relationshipDefinition = relationshipDefinition;
    }

    public DBEmbeddedRelationshipKeyField(FieldDescriptor<Key> fieldDescriptor, String persistentFieldName, DBKeyField relatedField, EntityRelationshipDefinition relationshipDefinition) {
        super(fieldDescriptor, persistentFieldName, relatedField);
        this.relationshipDefinition = relationshipDefinition;
    }

    public EntityRelationshipDefinition getRelationshipDefinition() {
        return relationshipDefinition;
    }
}
