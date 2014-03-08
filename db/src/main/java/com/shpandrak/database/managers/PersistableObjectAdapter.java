package com.shpandrak.database.managers;

import com.shpandrak.database.table.DBField;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.field.EntityKey;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 23:29
 */
public class PersistableObjectAdapter<T extends IPersistableShpandrakObject> extends ShpandrakObjectQueryConverter<T> {


    public PersistableObjectAdapter(DBTable dbTable, String tableAlias, ShpandrakObjectDescriptor objectDescriptor) {
        super(dbTable, tableAlias, objectDescriptor);
    }

    public List<List<Object>> prepareForPersisting(Collection<T> entities) {
        List<List<Object>> params = new ArrayList<List<Object>>(entities.size());
        for (T currEntity : entities){
            params.add(prepareForPersisting(currEntity));
        }
        return params;
    }

    public List<Object> prepareForPersisting(T entity) {
        List<DBField> dbFields = dbTable.getFields();
        List<Object> params = new ArrayList<Object>(dbFields.size());
        for (DBField currDBField : dbFields) {
            FieldDescriptor fieldDescriptor = currDBField.getFieldDescriptor();
            if (fieldDescriptor != null && fieldDescriptor.getObjectDescriptor() != null && fieldDescriptor.getObjectDescriptor().equals(entity.getObjectDescriptor())) {
                params.add(currDBField.prepareForPersisting(entity.getFieldsMap().get(fieldDescriptor.getName()).getValue()));
            }
        }

        return params;
    }

    public void generateKey(T entity) {
        //entity.setId(((KeyFieldDescriptor)(getObjectDescriptor().getOrderedFieldDescriptors().get(0))).generate());
        entity.setId(new EntityKey(UUID.randomUUID().toString()));
    }

    protected ShpandrakObjectDescriptor getObjectDescriptor() {
        return objectDescriptor;
    }


}
