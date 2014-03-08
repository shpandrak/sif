package com.shpandrak.persistence.managers;

import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.persistence.PersistenceException;

import java.util.Collection;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 23:00
 */
public interface IPersistableObjectManager<T extends IPersistableShpandrakObject> extends IReadOnlyManager<T> {

    /**
     * Persist a new object
     *
     * @param object object instance that is not previously persisted
     * @return instance of the object created
     * @throws PersistenceException
     */
    T create(T object) throws PersistenceException;

    /**
     * Persist multiple new objects
     *
     * @param objects objects collection that were not previously persisted
     * @return list of objects created
     * @throws PersistenceException
     */
    List<T> create(Collection<T> objects) throws PersistenceException;

    /**
     * Delete object
     * @param objectId the object id
     * @return an indication whether an actual delete occurred - false if the object were not previously persisted
     * @throws PersistenceException
     */
    boolean delete(Key objectId) throws PersistenceException;

    /**
     * Deletes multiple persistable objects
     * @param objectIds object ids whose we want to delete
     * @return number of objects actually deleted by the operation
     * @throws PersistenceException
     */
    int delete(Collection<Key> objectIds) throws PersistenceException;

    /**
     * Updates an already persisted object
     * @param object object to update
     * @throws PersistenceException
     */
    void update(T object) throws PersistenceException;

    /**
     * Updates a collection of existing objects
     * @param objects existing objects with id set
     * @throws PersistenceException
     */
    void update(Collection<T> objects) throws PersistenceException;

    /**
     * Update an objects field value
     * @param fieldDescriptor field to modify it's value
     * @param value actual value to update
     * @param id the object's id
     * @param <F> field type
     * @return number of rows affected by the operation
     * @throws PersistenceException
     */
    <F> int updateFieldValueById(FieldDescriptor<F> fieldDescriptor, F value, Key id) throws PersistenceException;

    <F1, F2> int updateFieldValuesById(FieldDescriptor<F1> fieldDescriptor1, F1 value1, FieldDescriptor<F2> fieldDescriptor2, F2 value2, Key id) throws PersistenceException;

    <F1, F2, F3> int updateFieldValuesById(FieldDescriptor<F1> fieldDescriptor1, F1 value1, FieldDescriptor<F2> fieldDescriptor2, F2 value2, FieldDescriptor<F3> fieldDescriptor3, F3 value3, Key id) throws PersistenceException;

    <F1, F2, F3, F4> int updateFieldValuesById(FieldDescriptor<F1> fieldDescriptor1, F1 value1, FieldDescriptor<F2> fieldDescriptor2, F2 value2, FieldDescriptor<F3> fieldDescriptor3, F3 value3, FieldDescriptor<F4> fieldDescriptor4, F4 value4, Key id) throws PersistenceException;

}
