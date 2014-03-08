package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.*;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.field.*;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.managers.IPersistableObjectManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/11/13
 * Time: 10:10
 */
public abstract class GDSBasePersistableObjectManagerBean<T extends IPersistableShpandrakObject> extends GDSBaseReadOnlyManagerBean<T> implements IPersistableObjectManager<T> {

    protected GDSBasePersistableObjectManagerBean() throws PersistenceException {
    }

    protected GDSBasePersistableObjectManagerBean(GDSConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    protected GDSPersistableObjectAdapter<T> getPersistObjectAdapter(String alias){
        return new GDSPersistableObjectAdapter<T>(getDescriptor(), alias);
    }

    @Override
    protected GDSQueryConverter<T> getQueryConverter() {
        return getPersistObjectAdapter(null);
    }

    @Override
    public T create(T object) throws PersistenceException {
        getPersistObjectAdapter(null).generateKey(object);
        Entity entity = getPersistObjectAdapter(null).prepareForPersisting(object);
        DatastoreService datastore = getDatastoreService();
        datastore.put(connectionProvider.getActiveTransaction(), entity);
        return object;
    }

    @Override
    public List<T> create(Collection<T> objects) throws PersistenceException {
        if (objects.isEmpty()) return Collections.emptyList();
        List<Entity> entityList = new ArrayList<Entity>(objects.size());
        for (T currObject : objects){
            //currObject.setId(((KeyFieldDescriptor)(getDescriptor().getOrderedFieldDescriptors().get(0))).generate());
            getPersistObjectAdapter(null).generateKey(currObject);
            entityList.add(getPersistObjectAdapter(null).prepareForPersisting(currObject));
        }
        DatastoreService datastore = getDatastoreService();
        datastore.put(connectionProvider.getActiveTransaction(), entityList);
        return new ArrayList<T>(objects);
    }

    @Override
    public boolean delete(Key objectId) throws PersistenceException {
        if (objectId == null) return false;
        DatastoreService datastoreService = getDatastoreService();
        datastoreService.delete(connectionProvider.getActiveTransaction(), KeyFactory.stringToKey(objectId.toString()));
        return true;
    }

    @Override
    public int delete(Collection<Key> objectIds) throws PersistenceException {
        if (objectIds == null || objectIds.isEmpty()) return 0;
        int size = objectIds.size();
        List<com.google.appengine.api.datastore.Key> dsKeys = new ArrayList<com.google.appengine.api.datastore.Key>(size);
        for (Key currKey : objectIds){
            dsKeys.add(KeyFactory.stringToKey(currKey.toString()));
        }
        getDatastoreService().delete(connectionProvider.getActiveTransaction(), dsKeys);
        return size;
    }

    @Override
    public void update(T object) throws PersistenceException {
        Entity entity = getPersistObjectAdapter(null).prepareForPersisting(object);
        DatastoreService datastore = getDatastoreService();
        datastore.put(connectionProvider.getActiveTransaction(), entity);
    }

    @Override
    public void update(Collection<T> objects) throws PersistenceException {
        List<Entity> entity = getPersistObjectAdapter(null).prepareForPersisting(objects);
        DatastoreService datastore = getDatastoreService();
        datastore.put(connectionProvider.getActiveTransaction(), entity);
    }

    @Override
    public <F> int updateFieldValueById(FieldDescriptor<F> fieldDescriptor, F value, Key id) throws PersistenceException {
        // In datastore we must get/put to update a single property
        T byId = getById(id);
        FieldInstance<F> fieldsInstance = byId.getFieldsInstance(fieldDescriptor);
        F previousValue = fieldsInstance.getValue();
        boolean hasChanged =
        (value == null && previousValue != null) ||
        (value != null && !value.equals(previousValue));

        if (hasChanged){
            fieldsInstance.setValue(value);
            update(byId);
            return 1;
        }else {
            return 0;
        }
    }

    @Override
    public <F1, F2> int updateFieldValuesById(FieldDescriptor<F1> fieldDescriptor1, F1 value1, FieldDescriptor<F2> fieldDescriptor2, F2 value2, Key id) throws PersistenceException {
        //todo:nicer in one go :)
        return
                updateFieldValueById(fieldDescriptor1, value1, id) +
                updateFieldValueById(fieldDescriptor2, value2, id);

    }

    @Override
    public <F1, F2, F3> int updateFieldValuesById(FieldDescriptor<F1> fieldDescriptor1, F1 value1, FieldDescriptor<F2> fieldDescriptor2, F2 value2, FieldDescriptor<F3> fieldDescriptor3, F3 value3, Key id) throws PersistenceException {
        //todo:nicer in one go :)
        return
                updateFieldValueById(fieldDescriptor1, value1, id) +
                updateFieldValueById(fieldDescriptor2, value2, id) +
                updateFieldValueById(fieldDescriptor3, value3, id);

    }

    @Override
    public <F1, F2, F3, F4> int updateFieldValuesById(FieldDescriptor<F1> fieldDescriptor1, F1 value1, FieldDescriptor<F2> fieldDescriptor2, F2 value2, FieldDescriptor<F3> fieldDescriptor3, F3 value3, FieldDescriptor<F4> fieldDescriptor4, F4 value4, Key id) throws PersistenceException {
        //todo:nicer in one go :)
        return
                updateFieldValueById(fieldDescriptor1, value1, id) +
                updateFieldValueById(fieldDescriptor2, value2, id) +
                updateFieldValueById(fieldDescriptor3, value3, id) +
                updateFieldValueById(fieldDescriptor4, value4, id);

    }
}
