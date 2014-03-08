package com.shpandrak.persistence.managers;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.datamodel.relationship.IRelationshipEntry;
import com.shpandrak.datamodel.relationship.RelationshipLoadLevel;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.query.filter.QueryFilter;
import com.shpandrak.persistence.query.filter.RelationshipLoadInstructions;

import java.util.List;
import java.util.Map;


/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 23:00
 */
public interface IEntityManager<T extends BaseEntity> extends IPersistableObjectManager<T> {

    /* List methods */

    List<T> list(RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException;

    List<T> list(QueryFilter filter, RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException;

    T findObject(QueryFilter filter, RelationshipLoadInstructions relationshipLoadInstructions) throws PersistenceException;

    /* getById methods */

    T getById(Key id, RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException;

    T getById(Key id, RelationshipLoadInstructions loadInstructions) throws PersistenceException;


    /* Relationship related methods */

    /**
     * Ensures a relationship is loaded at the desired relationship load level
     * @param entity the root entity
     * @param relationshipDefinition relationship definition for the relationship we want to load
     * @param relationshipLoadLevel load level for the relationship
     * @return true if actually loaded from data source and false if already existed in memory
     * @throws PersistenceException
     */
    boolean loadRelationship(T entity, EntityRelationshipDefinition relationshipDefinition, RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException;

    <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> List<REL_ENTRY_CLASS> listRelationshipEntries(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, Key entityId) throws PersistenceException;

    <TARGET_CLASS extends BaseEntity> List<TARGET_CLASS> listRelatedEntities(EntityRelationshipDefinition<T, TARGET_CLASS, ?> relationshipDefinition, Key entityId) throws PersistenceException;

    List<T> listByRelationShip(EntityRelationshipDefinition relationshipDefinition, Key relatedEntityId) throws PersistenceException;

    <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> void updateRelationship(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, Key entityId, REL_ENTRY_CLASS relationshipEntry) throws PersistenceException;


}
