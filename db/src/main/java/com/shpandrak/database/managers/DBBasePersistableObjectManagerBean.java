package com.shpandrak.database.managers;

import com.shpandrak.database.DBAccessService;
import com.shpandrak.database.DBAccessServiceBean;
import com.shpandrak.database.DBException;
import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.table.DBField;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.managers.IPersistableObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 23:00
 */
public abstract class DBBasePersistableObjectManagerBean<T extends IPersistableShpandrakObject> extends DBBaseReadOnlyManagerBean<T> implements IPersistableObjectManager<T> {
    private static final Logger logger = LoggerFactory.getLogger(DBBaseReadOnlyManagerBean.class);
    protected DBAccessService dbAccessService;

    protected PersistableObjectAdapter<T> getPersistEntityAdapter(String tableAlias){
        return new PersistableObjectAdapter<T>(getDbTable(), tableAlias, getDescriptor());
    }

    protected PersistableObjectAdapter<T> getPersistEntityAdapter(){
        return getPersistEntityAdapter(null);
    }


    protected DBBasePersistableObjectManagerBean(DBTable dbTable, IDBConnectionProvider connectionProvider) {
        super(dbTable, connectionProvider, new DBAccessServiceBean(connectionProvider));
        this.dbAccessService = (DBAccessService) getQueryService();
    }

    @Override
    public T create(T object) throws PersistenceException {
        if (object == null){
            throw new IllegalArgumentException("Cannot create null object");
        }
        if (object.getId() != null){
            throw new IllegalArgumentException("Cannot create an object with an existing Id" + object.getClass().getSimpleName() + " with id " + object.getId());
        }

        // Generating the entityKey
        getPersistEntityAdapter().generateKey(object);

        // Inserting to db
        dbAccessService.executeUpdate(getDbTable().getInsertSQL(), getPersistEntityAdapter().prepareForPersisting(object));

        return object;
    }


    @Override
    public void update(T object) throws PersistenceException {
        if (object == null){
            throw new IllegalArgumentException("Cannot update null object");
        }
        if (object.getId() == null){
            throw new IllegalArgumentException("Cannot update an object with no existing Id" + object.getClass().getSimpleName());
        }

        // Inserting to db
        List<Object> params = getPersistEntityAdapter().prepareForPersisting(object);

        // push id param to tail
        params.add( params.remove(0));

        // Execute the update
        dbAccessService.executeUpdate(getDbTable().getUpdateSQL(), params);
    }

    @Override
    public void update(Collection<T> objects) throws PersistenceException {
        if (objects == null){
            throw new IllegalArgumentException("Cannot update null objects list");
        }

        // Inserting to db
        List<List<Object>> bulkParams = getPersistEntityAdapter().prepareForPersisting(objects);

        // push id param to tail
        for (List<Object> currParams : bulkParams){
            currParams.add( currParams.remove(0));
        }

        // Execute the update
        dbAccessService.executeUpdates(getDbTable().getUpdateSQL(), bulkParams);

    }

    @Override
    public <F> int updateFieldValueById(FieldDescriptor<F> fieldDescriptor, F value, Key id) throws PersistenceException {
        DBField<F> dbField = dbFieldFromField(fieldDescriptor);
        if (dbField == null){
            throw new IllegalStateException("Unsupported persistent field " + fieldDescriptor.toString());
        }

        return dbAccessService.executeUpdate("update " + dbTable.getName() + " set " + dbField.getPersistentFieldName() + "=? where id=?", Arrays.<Object>asList(dbField.prepareForPersisting(value), dbTable.getFields().get(0).prepareForPersisting(id)));
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

    @Override
    public List<T> create(Collection<T> objects) throws PersistenceException {
        if (objects == null) throw new IllegalArgumentException("Unable to persist null list");
        if (objects.isEmpty()) return Collections.emptyList();
        PersistableObjectAdapter<T> adapter = getPersistEntityAdapter();

        List<List<Object>> params = new ArrayList<List<Object>>(objects.size());
        for (T currObject : objects){
            if (currObject == null){
                throw new IllegalArgumentException("Cannot create null object");
            }
            if (currObject.getId() != null){
                throw new IllegalArgumentException("Cannot create an object with an existing Id" + currObject.getClass().getSimpleName() + " with id " + currObject.getId());
            }

            adapter.generateKey(currObject);
            params.add(adapter.prepareForPersisting(currObject));
        }

        // Inserting to db
        dbAccessService.executeUpdates(dbTable.getInsertSQL(), params);

        return new ArrayList<T>(objects);


    }

    protected <E extends IPersistableShpandrakObject> void insertEntities(Collection<E> objects, DBTable dbTable, PersistableObjectAdapter<E> adapter) throws DBException {
        if (objects == null || objects.isEmpty()) return;

        ArrayList<List<Object>> params = new ArrayList<List<Object>>(objects.size());
        for (E currObject : objects){
            adapter.generateKey(currObject);
            params.add(adapter.prepareForPersisting(currObject));
        }

        // Inserting to db
        dbAccessService.executeUpdates(dbTable.getInsertSQL(), params);

    }

    @Override
    public boolean delete(Key entityId) throws PersistenceException {
        if (entityId == null){
            throw new IllegalArgumentException("Illegal entity id: null, bye now...");
        }

        int rowsDeleted = deleteFromTableById(entityId);
        return rowsDeleted > 0;
    }

    @Override
    public int delete(Collection<Key> objectIds) throws PersistenceException {
        int rowsCount = 0;
        List<List<Object>> multiParams = new ArrayList<List<Object>>(objectIds.size());
        for (Key currId : objectIds){
            multiParams.add(Arrays.<Object>asList(currId));
        }
        List<Integer> rows = dbAccessService.executeUpdates(dbTable.getDeleteByIdSQL(), multiParams);
        for (Integer currCount : rows){
            rowsCount += currCount;
        }
        return rowsCount;
    }

    protected int deleteFromTableById(Key entityId) throws DBException {
        return dbAccessService.executeUpdate(getDbTable().getDeleteByIdSQL(), DBUtil.keyAsParams(entityId));
    }

    @Override
    protected IQueryConverter<T> getQueryConverter(String tableAlias) {
        return getPersistEntityAdapter(tableAlias);
    }


}
