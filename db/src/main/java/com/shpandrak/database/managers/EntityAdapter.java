package com.shpandrak.database.managers;

import com.shpandrak.database.table.DBEmbeddedRelationshipKeyField;
import com.shpandrak.database.table.DBField;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.BaseEntityDescriptor;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.FieldInstance;
import com.shpandrak.datamodel.relationship.EntityOneToManyRelationship;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/21/12
 * Time: 11:42
 */
public class EntityAdapter<T extends BaseEntity> extends PersistableObjectAdapter<T> {
    public EntityAdapter(DBTable dbTable, String tableAlias, BaseEntityDescriptor<T> objectDescriptor) {
        super(dbTable, tableAlias, objectDescriptor);
    }

    @Override
    public T convert(ResultSet resultSet) throws SQLException {
        List<DBField> dbFields = dbTable.getFields();
        T instance = (T) objectDescriptor.instance();
        Map<String,FieldInstance> fieldsMap = instance.getFieldsMap();
        for (DBField currDBField : dbFields) {
            FieldDescriptor fieldDescriptor = currDBField.getFieldDescriptor();
            ShpandrakObjectDescriptor currObjectDescriptor = fieldDescriptor.getObjectDescriptor();
            if (currObjectDescriptor != null && currObjectDescriptor.equals(objectDescriptor)){
                fieldsMap.get(fieldDescriptor.getName()).setValue(currDBField.convert(resultSet, tableAlias));
            }else if (currDBField instanceof DBEmbeddedRelationshipKeyField) {
                DBEmbeddedRelationshipKeyField embeddedRelationshipUUIDField = (DBEmbeddedRelationshipKeyField) currDBField;
                ((EntityOneToManyRelationship)instance.getRelationship(embeddedRelationshipUUIDField.getRelationshipDefinition())).setTargetEntityId(embeddedRelationshipUUIDField.convert(resultSet, tableAlias));
            }
        }
        return instance;
    }

    @Override
    public List<Object> prepareForPersisting(T entity) {
        List<DBField> dbFields = dbTable.getFields();
        List<Object> params = new ArrayList<Object>(dbFields.size());
        for (DBField currDBField : dbFields) {
            FieldDescriptor fieldDescriptor = currDBField.getFieldDescriptor();
            ShpandrakObjectDescriptor currObjectDescriptor = fieldDescriptor.getObjectDescriptor();
            if (currObjectDescriptor != null && currObjectDescriptor.equals(objectDescriptor)){
                params.add(currDBField.prepareForPersisting(entity.getFieldsMap().get(fieldDescriptor.getName()).getValue()));
            }else if (currDBField instanceof DBEmbeddedRelationshipKeyField) {
                DBEmbeddedRelationshipKeyField embeddedRelationshipUUIDField = (DBEmbeddedRelationshipKeyField) currDBField;
                params.add(embeddedRelationshipUUIDField.prepareForPersisting(((EntityOneToManyRelationship) entity.getRelationship(embeddedRelationshipUUIDField.getRelationshipDefinition())).getTargetEntityId()));
            }
        }

        return params;
    }

    @Override
    protected BaseEntityDescriptor<T> getObjectDescriptor() {
        return (BaseEntityDescriptor<T>) super.getObjectDescriptor();
    }
}
