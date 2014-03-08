package com.shpandrak.persistence.managers;


import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.query.filter.QueryFilter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:09
 */
public interface IReadOnlyManager<T> {

    /* List methods */

    /**
     * List objects applying to a supplied filter
     * @param filter filter object
     * @return list of found objects satisfying the filter
     * @throws PersistenceException
     */
    List<T> list(QueryFilter filter);

    /**
     * List all available objects
     * @return list of all available objects
     * @throws PersistenceException
     */
    List<T> list();

    /**
     * List all objects matching a given value for one of the object's fields
     * @param field object field to use for filtering
     * @param value object filed value we want to query for
     * @param <F> the field's type
     * @return list of objects satisfying the given field filter
     * @throws PersistenceException
     */
    <F> List<T> listByField(FieldDescriptor<F> field, F value) throws PersistenceException;

    /* getById methods */

    /**
     * Returns an object by it's Id
     *
     * @param id object id
     * @return object identified by the supplied id parameter
     * @throws PersistenceException
     */
    T getById(Key id) throws PersistenceException;

    List<T> getByIds(Collection<Key> ids);

    /**
     * get all available objects in a map using the object Id as key
     * @return map by object id
     * @throws PersistenceException
     */
    Map<Key, T> getMapById() throws PersistenceException;

    /**
     * get all objects satisfying the given filter in a map using the object Id as key
     *
     * @param filter filter for the object
     * @return map by object id
     * @throws PersistenceException
     */
    Map<Key, T> getMapById(QueryFilter filter) throws PersistenceException;

    /**
     * get all available objects in a map using a specific object filed as map key
     * @param field field descriptor to use for the map key
     * @param <F> field type
     * @return map by field value
     * @throws PersistenceException
     */
    <F> Map<F, T> getMapByField(FieldDescriptor<F> field) throws PersistenceException;

    /**
     * get objects matching query filter in a map using a specific object filed as map key
     * @param field field descriptor to use for the map key
     * @param <F> field type
     * @param filter filter to use
     * @return map by field value
     * @throws PersistenceException
     */
    <F> Map<F, T> getMapByField(FieldDescriptor<F> field, QueryFilter filter) throws PersistenceException;

    /**
     * Return a single object according to a specific filed value expecting single object or none as a result
     * @param field filed descriptor to use
     * @param value filed value
     * @param <F> filed type
     * @return object instance or null
     * @throws PersistenceException for any data access error or in case more than one result is returned by field value
     */
    <F> T getByField(FieldDescriptor<F> field, F value);

    /**
     * List the values of one of the object's fields
     * @param fieldDescriptor field to query
     * @param filter filter
     * @param <F> field type
     * @return list of field values
     */
    <F> Collection<F> listFieldValues(FieldDescriptor<F> fieldDescriptor, QueryFilter filter);

    /**
     * List object id's matching the given filter
     *
     * @param filter filter to us
     * @return list of object ids matching the filter
     */
    Set<Key> listIds(QueryFilter filter);

    Key getObjectId(QueryFilter filter);

    T findObject(QueryFilter filter);

    long count();

    long count(QueryFilter filter);

    <F> long countByField(FieldDescriptor<F> fieldDescriptor, F value);

}
