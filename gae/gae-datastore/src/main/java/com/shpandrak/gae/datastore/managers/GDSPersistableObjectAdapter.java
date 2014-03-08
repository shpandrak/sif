package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.shpandrak.common.model.FieldType;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.field.EntityKey;
import com.shpandrak.datamodel.field.FieldInstance;
import com.shpandrak.persistence.PersistenceException;


import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/11/13
 * Time: 10:15
 */
public class GDSPersistableObjectAdapter<T extends IPersistableShpandrakObject> extends GDSQueryConverterImpl<T> {
    public GDSPersistableObjectAdapter(BasePersistentObjectDescriptor<T> descriptor, String alias) {
        super(descriptor);
    }

    protected List<Entity> prepareForPersisting(Collection<T> objects){
        List<Entity> entities = new ArrayList<Entity>(objects.size());
        for (T currObject : objects){
            entities.add(prepareForPersisting(currObject));
        }
        return entities;
    }

    protected Entity prepareForPersisting(T object){
        Entity entity = new Entity(KeyFactory.stringToKey(object.getId().toString()));
        Map<String,FieldInstance> fieldsMap = object.getFieldsMap();
        for (Map.Entry<String, FieldInstance> currFieldEntry : fieldsMap.entrySet()) {
            if (!"id".equals(currFieldEntry.getKey())){
                Object value = getFieldForGDS(currFieldEntry.getValue().getFieldType(), currFieldEntry.getValue().getValue());
                entity.setProperty(currFieldEntry.getKey(), value);
            }
        }

        return entity;

    }

    public static Object getFieldForGDS(FieldType fieldType, Object originalValue) {
        Object value;
        switch (fieldType){
            case ENUM:
                value = originalValue == null? null : originalValue.toString();
                break;
            case KEY:
                String keyString = null;
                if (originalValue != null){
                    keyString = originalValue.toString();
                }
                value = KeyFactory.stringToKey(keyString);
                break;
            default:
                value = originalValue;
                break;
        }
        return value;
    }

    public static Class getClassByFieldType(FieldType fieldType) {
        Object value;
        switch (fieldType){
            case ENUM:
                return String.class;
            case KEY:
                return Key.class;
            default:
                return fieldType.getJavaType();
        }
    }

    public void generateKey(T currObject) throws PersistenceException {
        // For applicative key fields - using the field to generate the gds key
        String keyName = generateKeyName(currObject);
        currObject.setId(new EntityKey(KeyFactory.createKeyString(getDescriptor().getEntityClass().getSimpleName(), keyName)));
    }

    protected String generateKeyName(T currObject) {
        String keyName;
        if (getDescriptor().getAppKeyFieldDescriptor() != null){
            keyName = currObject.getFieldsInstance(getDescriptor().getAppKeyFieldDescriptor()).stringValue();
        }else {
            keyName = UUID.randomUUID().toString();
        }
        return keyName;
    }
}
