package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.*;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.field.EntityKey;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.gae.datastore.query.GDSQueryFilterParser;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.PersistenceLayerManager;
import com.shpandrak.persistence.managers.IReadOnlyManager;
import com.shpandrak.persistence.query.filter.BasicFieldFilterCondition;
import com.shpandrak.persistence.query.filter.FilterConditionOperatorType;
import com.shpandrak.persistence.query.filter.QueryFilter;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/10/13
 * Time: 20:34
 */
public abstract class GDSBaseReadOnlyManagerBean<T extends IPersistableShpandrakObject> implements IReadOnlyManager<T> {

    protected GDSConnectionProvider connectionProvider;

    protected abstract Class<T> getEntityClass();

    protected abstract BasePersistentObjectDescriptor<T> getDescriptor();

    protected GDSBaseReadOnlyManagerBean() throws PersistenceException {
        this((GDSConnectionProvider) PersistenceLayerManager.getConnectionProvider());
    }

    protected GDSBaseReadOnlyManagerBean(GDSConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected GDSQueryConverter<T> getQueryConverter(){
        return new GDSQueryConverterImpl<T>(getDescriptor());
    }

    @Override
    public List<T> list(QueryFilter filter) throws PersistenceException {
        DatastoreService datastore = getDatastoreService();
        PreparedQuery pq = prepareQuery(datastore, filter);


        ArrayList<T> list = new ArrayList<T>();
        Iterable<Entity> entities = pq.asIterable();
        for (Entity currEntity : entities) {
            list.add(getQueryConverter().convert(currEntity));
        }
        return list;
    }

    private Query buildQuery(QueryFilter filter) throws PersistenceException {
        // Use class Query to assemble a query
        GDSQueryFilterParser queryFilterParser = new GDSQueryFilterParser(filter, getDescriptor());
        queryFilterParser.parse();
        return queryFilterParser.getQuery();
    }

    private PreparedQuery prepareQuery(DatastoreService datastoreService, QueryFilter filter) throws PersistenceException {
        return datastoreService.prepare(connectionProvider.getActiveTransaction(), buildQuery(filter));
    }

    protected DatastoreService getDatastoreService() {
        // Get the Datastore Service
        return connectionProvider.getDatastoreService();
    }

    @Override
    public List<T> list() throws PersistenceException {
        return list(null);
    }

    @Override
    public <F> List<T> listByField(FieldDescriptor<F> field, F value) throws PersistenceException {
        return list(new QueryFilter(BasicFieldFilterCondition.build(field, FilterConditionOperatorType.EQUALS, value)));
    }

    @Override
    public T getById(Key id) throws PersistenceException {
        DatastoreService datastoreService = getDatastoreService();
        try {
            Entity entity = datastoreService.get(connectionProvider.getActiveTransaction(), KeyFactory.stringToKey(id.toString()));
            return getQueryConverter().convert(entity);
        } catch (EntityNotFoundException e) {
            throw new PersistenceException("Entity with id " + id.toString() + " was not found", e);
        }
    }

    @Override
    public List<T> getByIds(Collection<Key> ids) throws PersistenceException {
        DatastoreService datastoreService = getDatastoreService();
        List<com.google.appengine.api.datastore.Key> gdsIds = new ArrayList<com.google.appengine.api.datastore.Key>(ids.size());
        for (Key currKey : ids){
            gdsIds.add(KeyFactory.stringToKey(currKey.toString()));
        }
        List<T> list = new ArrayList<T>(ids.size());
        Map<com.google.appengine.api.datastore.Key, Entity> entities = datastoreService.get(connectionProvider.getActiveTransaction(), gdsIds);
        for (Entity currEntity : entities.values()) {
            list.add(getQueryConverter().convert(currEntity));
        }

        return list;
    }

    @Override
    public Map<Key, T> getMapById() throws PersistenceException {
        return getMapById(null);
    }

    @Override
    public Map<Key, T> getMapById(QueryFilter filter) throws PersistenceException {
        return getMapByField(getDescriptor().getKeyFieldDescriptor(), filter);
    }

    @Override
    public <F> Map<F, T> getMapByField(FieldDescriptor<F> field) throws PersistenceException {
        return getMapByField(field, null);
    }

    @Override
    public <F> Map<F, T> getMapByField(FieldDescriptor<F> field, QueryFilter filter) throws PersistenceException {
        List<T> list = list(filter);
        Map<F, T> retVal = new HashMap<F, T>(list.size());
        for (T currEntity : list){
            F fieldValue = currEntity.getFieldsInstance(field).getValue();
            T oldValue = retVal.put(fieldValue, currEntity);
            if (oldValue != null){
                throw new IllegalStateException("Two values with same key fetched for key " + fieldValue + " entities: " + oldValue.toString() + ", " + currEntity.toString());
            }
        }
        return retVal;
    }

    @Override
    public <F> T getByField(FieldDescriptor<F> field, F value) throws PersistenceException {
        DatastoreService datastore = getDatastoreService();
        PreparedQuery pq = prepareQuery(datastore, new QueryFilter(BasicFieldFilterCondition.build(field, FilterConditionOperatorType.EQUALS, value)));
        Entity singleEntity = pq.asSingleEntity();
        if (singleEntity == null) return null;
        return getQueryConverter().convert(singleEntity);
    }

    @Override
    public <F> Collection<F> listFieldValues(FieldDescriptor<F> fieldDescriptor, QueryFilter filter) throws PersistenceException {
        DatastoreService datastore = getDatastoreService();
        Query query = buildQuery(filter);

        // Adding projection
        query.addProjection(new PropertyProjection(fieldDescriptor.getName(), GDSEntityAdapter.getClassByFieldType(fieldDescriptor.getFieldType())));
        PreparedQuery pq = datastore.prepare(connectionProvider.getActiveTransaction(), query);
        List<F> retVal = new ArrayList<F>();
        for (Entity currKeyEntity : pq.asIterable()){
            retVal.add((F) GDSEntityAdapter.getFieldForGDS(fieldDescriptor.getFieldType(), currKeyEntity.getProperty(fieldDescriptor.getName())));
        }
        return retVal;
    }

    @Override
    public Set<Key> listIds(QueryFilter filter) throws PersistenceException {
        DatastoreService datastore = getDatastoreService();
        Query query = buildQuery(filter);
        query.setKeysOnly();
        PreparedQuery pq = datastore.prepare(connectionProvider.getActiveTransaction(), query);
        Set<Key> retVal = new HashSet<Key>();
        for (Entity currKeyEntity : pq.asIterable()){
            retVal.add(new EntityKey(KeyFactory.keyToString(currKeyEntity.getKey())));
        }
        return retVal;

    }

    @Override
    public Key getObjectId(QueryFilter filter) throws PersistenceException {
        DatastoreService datastore = getDatastoreService();
        Query query = buildQuery(filter);
        query.setKeysOnly();
        PreparedQuery pq = datastore.prepare(connectionProvider.getActiveTransaction() , query);
        Entity keyEntity = pq.asSingleEntity();
        if (keyEntity == null) return null;
        return new EntityKey(KeyFactory.keyToString(keyEntity.getKey()));
    }

    @Override
    public T findObject(QueryFilter filter) throws PersistenceException {
        DatastoreService datastore = getDatastoreService();
        Query query = buildQuery(filter);
        PreparedQuery pq = datastore.prepare(connectionProvider.getActiveTransaction() , query);
        Entity singleEntity = pq.asSingleEntity();
        if (singleEntity == null) return null;
        return getQueryConverter().convert(singleEntity);
    }

    @Override
    public long count() throws PersistenceException {
        return count(null);
    }

    @Override
    public long count(QueryFilter filter) throws PersistenceException {
        DatastoreService datastore = getDatastoreService();
        Query query = buildQuery(filter);
        query.setKeysOnly();
        PreparedQuery pq = datastore.prepare(connectionProvider.getActiveTransaction(), query);
        return pq.countEntities(FetchOptions.Builder.withDefaults());
    }

    @Override
    public <F> long countByField(FieldDescriptor<F> fieldDescriptor, F value) throws PersistenceException {
        return count(new QueryFilter(BasicFieldFilterCondition.build(fieldDescriptor, FilterConditionOperatorType.EQUALS, value)));
    }
}
