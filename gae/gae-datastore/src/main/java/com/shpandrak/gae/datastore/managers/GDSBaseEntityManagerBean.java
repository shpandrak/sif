package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.*;
import com.shpandrak.datamodel.*;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.relationship.*;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.managers.EntityRelationshipValidator;
import com.shpandrak.persistence.managers.IEntityManager;
import com.shpandrak.persistence.managers.IPersistableObjectManager;
import com.shpandrak.persistence.query.filter.QueryFilter;
import com.shpandrak.persistence.query.filter.RelationshipFilterCondition;
import com.shpandrak.persistence.query.filter.RelationshipLoadInfo;
import com.shpandrak.persistence.query.filter.RelationshipLoadInstructions;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/11/13
 * Time: 10:29
 */
public abstract class GDSBaseEntityManagerBean <T extends BaseEntity> extends GDSBasePersistableObjectManagerBean<T> implements IEntityManager<T> {

    protected GDSBaseEntityManagerBean() {
    }

    protected GDSBaseEntityManagerBean(GDSConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    protected BaseEntityDescriptor<T> getEntityDescriptor() {
        return EntityDescriptorFactory.get(getEntityClass());
    }

    @Override
    protected BasePersistentObjectDescriptor<T> getDescriptor() {
        return getEntityDescriptor();
    }

    @Override
    protected GDSPersistableObjectAdapter<T> getPersistObjectAdapter(String alias) {
        return new GDSEntityAdapter<T>(getEntityDescriptor(), alias);
    }

    @Override
    public T create(T entity)  {
        // Validating Entity relationships
        EntityRelationshipValidator.validateRelationshipsForCreate(entity);

        // Inserting to db
        T createdEntity = super.create(entity);

        // Persisting relationships
        createRelationships(createdEntity);

        return createdEntity;
    }

    @Override
    public List<T> create(Collection<T> entities)  {
        // Validating Entities relationships
        for (T currEntity : entities){
            EntityRelationshipValidator.validateRelationshipsForCreate(currEntity);
        }

        // Bulk saving
        List<T> createdEntities = super.create(entities);

        // Persisting relationships
        //todo: can and must bulk inserts here!!!

        for (T currEntity : createdEntities){
            createRelationships(currEntity);
        }

        return createdEntities;

    }


    private void createRelationships(T entity)  {

        // For every relationship we gather the sql parameters for bulk insertion
        List<EntityRelationship> entityRelationships = entity.getLoadedRelationships();
        for (EntityRelationship currRelationship : entityRelationships) {
            switch (currRelationship.getDefinition().getType()) {
                case MANY_TO_MANY:
                    EntityManyToManyRelationship manyToManyRelationship = (EntityManyToManyRelationship) currRelationship;
                    IPersistableObjectManager relationshipEntryManager = GDSManagerFactory.getManager(((BasePersistentObjectDescriptor)manyToManyRelationship.getDefinition().getRelationshipEntryDescriptor()).getEntityClass(), connectionProvider);
                    relationshipEntryManager.create(manyToManyRelationship.getRelationshipEntriesByTargetEntityId().values());
                    break;
            }
        }

    }


    private RelationshipLoadInstructions getRelationshipLoadInstructions(RelationshipLoadLevel relationshipLoadLevel) {
        RelationshipLoadInstructions loadInstructions = null;

        switch (relationshipLoadLevel) {
            case NONE:
                break;
            case ID:
            case FULL:
                ArrayList<RelationshipLoadInfo> loadInfo = new ArrayList<RelationshipLoadInfo>(getEntityDescriptor().getRelationshipDefinitionMap().size());
                for (EntityRelationshipDefinition currRelDef : getEntityDescriptor().getRelationshipDefinitionMap().values()) {
                    loadInfo.add(new RelationshipLoadInfo(currRelDef, relationshipLoadLevel));
                }
                loadInstructions = new RelationshipLoadInstructions(loadInfo);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Relationship load level: " + relationshipLoadLevel);
        }
        return loadInstructions;
    }

    @Override
    public List<T> list(RelationshipLoadLevel relationshipLoadLevel)  {
        return list(getRelationshipLoadInstructions(relationshipLoadLevel));

    }

    private List<T> list(RelationshipLoadInstructions relationshipLoadInstructions)  {
        List<T> list = list();
        if (list.isEmpty()) return list;

        loadRelationships(relationshipLoadInstructions, list);

        return list;

    }

    private void loadRelationships(RelationshipLoadInstructions relationshipLoadInstructions, List<T> list)  {
        if (relationshipLoadInstructions != null) {
            for (RelationshipLoadInfo currRelationshipDefEntry : relationshipLoadInstructions.getLoadInfo()) {
                EntityRelationshipDefinition relationshipDef = currRelationshipDefEntry.getRelationshipDefinition();
                loadRelationship(list, relationshipDef, currRelationshipDefEntry.getLoadLevel());
            }
        }
    }

    private void loadRelationship(List<T> list, EntityRelationshipDefinition relationshipDefinition, RelationshipLoadLevel relationshipLoadLevel)  {
        switch (relationshipLoadLevel) {
            case NONE:
                break;
            case ID:
                switch (relationshipDefinition.getType()) {
                    case ONE_TO_MANY:
                        // Id gets loaded for free...
                        break;
                    case MANY_TO_MANY:
                        loadManyToManyRelationshipWithIdLevel(relationshipDefinition, list);
                        break;
                    case MANY_TO_ONE:
                        loadManyToOneRelationshipWithIdLevel(relationshipDefinition, list);
                        break;

                    case ONE_TO_ONE:
                        //todo:
                }
                break;
            case FULL:

                switch (relationshipDefinition.getType()) {
                    case ONE_TO_MANY:
                        loadOneToManyRelationship(relationshipDefinition, list);
                        break;
                    case MANY_TO_MANY:
                        loadManyToManyRelationshipFullLevel(relationshipDefinition, list);
                        break;
                    case MANY_TO_ONE:
                        loadManyToOneRelationshipWithFullLevel(relationshipDefinition, list);
                    case ONE_TO_ONE:
                        //todo:
                }
                break;

        }
    }

    protected void loadOneToManyRelationship(EntityRelationshipDefinition relationshipDef, List<T> list)  {
        Class targetClass = relationshipDef.getTargetClassType();
        GDSBaseReadOnlyManagerBean targetClassManager = (GDSBaseReadOnlyManagerBean) GDSManagerFactory.getManager(targetClass, connectionProvider);
        for (T currEntity : list){
            EntityOneToManyRelationship oneToManyRelationship = currEntity.getOneToManyRelationship(relationshipDef);
            Key targetEntityId = oneToManyRelationship.getTargetEntityId();
            if (targetEntityId != null){
                //todo:bulk...
                oneToManyRelationship.setTargetEntity((BaseEntity) targetClassManager.getById(targetEntityId));
            }else {
                oneToManyRelationship.setTargetEntity(null);
            }
        }
    }

    protected void loadManyToOneRelationshipWithFullLevel(EntityRelationshipDefinition relationshipDefinition, List<T> list)  {
        EntityRelationshipDefinition reverseRelationshipDefinition = relationshipDefinition.getReverseRelationshipDefinition();
        if (reverseRelationshipDefinition == null){
            throw new IllegalStateException("Only two-side relationship allowed for many to one relationship (todo)");
            //todo:support that...
        }


        GDSBaseReadOnlyManagerBean targetClassManager = (GDSBaseReadOnlyManagerBean) GDSManagerFactory.getManager(reverseRelationshipDefinition.getSourceClassType(), connectionProvider);


        // todo:bulk loading!!!!
        for (T currEntity : list){
            QueryFilter queryFilter = new QueryFilter(new RelationshipFilterCondition(reverseRelationshipDefinition, currEntity.getId()), null, null, relationshipDefinition.getRelationshipSort());
            List targetEntities = targetClassManager.list(queryFilter);
            ((EntityManyToOneRelationship) currEntity.getRelationship(relationshipDefinition)).setFull(targetEntities);
        }
    }

    protected void loadManyToOneRelationshipWithIdLevel(EntityRelationshipDefinition relationshipDefinition, List<T> list)  {
        EntityRelationshipDefinition reverseRelationshipDefinition = relationshipDefinition.getReverseRelationshipDefinition();
        if (reverseRelationshipDefinition == null){
            throw new IllegalStateException("Only two-side relationship allowed for many to one relationship (todo)");
            //todo:support that...
        }

        GDSBaseReadOnlyManagerBean targetClassManager = (GDSBaseReadOnlyManagerBean) GDSManagerFactory.getManager(reverseRelationshipDefinition.getSourceClassType(), connectionProvider);

        // todo:bulk loading!!!!
        for (T currEntity : list){
            Set<Key> targetIds = targetClassManager.listIds(new QueryFilter(new RelationshipFilterCondition(reverseRelationshipDefinition, currEntity.getId()), null, null, relationshipDefinition.getRelationshipSort()));
            ((EntityManyToOneRelationship) currEntity.getRelationship(relationshipDefinition)).setIds(targetIds);
        }

    }

    protected void loadManyToManyRelationshipFullLevel(EntityRelationshipDefinition relationshipDef, List<T> list)  {
        DatastoreService datastoreService = getDatastoreService();
        GDSBasePersistableObjectManagerBean<BasePersistableRelationshipEntry> relationshipEntryManager = (GDSBasePersistableObjectManagerBean<BasePersistableRelationshipEntry>) getRelationshipManager(relationshipDef.getRelationshipEntryDescriptor());
        for (T currEntity : list){
            PreparedQuery preparedQuery = datastoreService.prepare(connectionProvider.getActiveTransaction(), new Query(relationshipDef.getRelationshipEntryDescriptor().getEntityClass().getSimpleName(), KeyFactory.stringToKey(currEntity.getId().toString())));
            QueryResultList<Entity> entities = preparedQuery.asQueryResultList(FetchOptions.Builder.withDefaults());

            List<BasePersistableRelationshipEntry> relationshipEntries = new ArrayList<BasePersistableRelationshipEntry>(entities.size());
            for (Entity currEntityKey : entities){
                BasePersistableRelationshipEntry entry = relationshipEntryManager.getPersistObjectAdapter(null).convert(currEntityKey);
                //todo:bulk load by id
                entry.setTargetEntity((BaseEntity) GDSManagerFactory.getManager(relationshipDef.getTargetClassType()).getById(entry.getTargetEntityId()));
                relationshipEntries.add(entry);

            }
            currEntity.getManyToManyRelationship(relationshipDef).set(RelationshipLoadLevel.FULL, relationshipEntries);

        }

    }


    private GDSBasePersistableObjectManagerBean<? extends BasePersistableRelationshipEntry> getRelationshipManager(ShpandrakObjectDescriptor relationshipEntryDescriptor)  {
        return (GDSBasePersistableObjectManagerBean<? extends BasePersistableRelationshipEntry>) GDSManagerFactory.getManager(((BasePersistentObjectDescriptor)relationshipEntryDescriptor).getEntityClass(), connectionProvider);
    }

    protected void loadManyToManyRelationshipWithIdLevel(EntityRelationshipDefinition relationshipDef, List<T> list)  {
        DatastoreService datastoreService = getDatastoreService();
        GDSBasePersistableObjectManagerBean<? extends BasePersistableRelationshipEntry> relationshipEntryManager = getRelationshipManager(relationshipDef.getRelationshipEntryDescriptor());
        for (T currEntity : list){
            PreparedQuery preparedQuery = datastoreService.prepare(connectionProvider.getActiveTransaction(), new Query(relationshipDef.getRelationshipEntryDescriptor().getEntityClass().getSimpleName(), KeyFactory.stringToKey(currEntity.getId().toString())));
            QueryResultList<Entity> entities = preparedQuery.asQueryResultList(FetchOptions.Builder.withDefaults());

            List<BasePersistableRelationshipEntry> relationshipEntries = new ArrayList<BasePersistableRelationshipEntry>(entities.size());
            for (Entity currEntityKey : entities){
                relationshipEntries.add(relationshipEntryManager.getPersistObjectAdapter(null).convert(currEntityKey));

            }
            currEntity.getManyToManyRelationship(relationshipDef).set(RelationshipLoadLevel.ID, relationshipEntries);

        }
    }


    @Override
    public List<T> list(QueryFilter filter, RelationshipLoadLevel relationshipLoadLevel)  {
        List<T> list = list(filter);
        RelationshipLoadInstructions relationshipLoadInstructions = getRelationshipLoadInstructions(relationshipLoadLevel);
        loadRelationships(relationshipLoadInstructions, list);
        return list;
    }

    @Override
    public T findObject(QueryFilter filter, RelationshipLoadInstructions relationshipLoadInstructions)  {
        T object = findObject(filter);
        loadRelationships(relationshipLoadInstructions, Arrays.<T>asList(object));
        return object;
    }

    @Override
    public T getById(Key id, RelationshipLoadLevel relationshipLoadLevel)  {
        T entity = getById(id);
        RelationshipLoadInstructions relationshipLoadInstructions = getRelationshipLoadInstructions(relationshipLoadLevel);
        return getById (id, relationshipLoadInstructions);
    }

    @Override
    public T getById(Key id, RelationshipLoadInstructions loadInstructions)  {
        T entity = getById(id);
        loadRelationships(loadInstructions, Arrays.<T>asList(entity));
        return entity;
    }

    @Override
    public boolean loadRelationship(T entity, EntityRelationshipDefinition relationshipDefinition, RelationshipLoadLevel relationshipLoadLevel)  {
        if (relationshipLoadLevel == RelationshipLoadLevel.NONE) throw new IllegalArgumentException("Cannot load relationship with \"None\" level for relationship " + relationshipDefinition.toString());
        EntityRelationship relationship = entity.getRelationship(relationshipDefinition);
        switch (relationship.getLoadLevel()){
            case FULL:
                return false;
            case ID:
                switch (relationshipLoadLevel){
                    case ID:
                        return false;
                    default:
                        break;
                }
                break;
        }

        loadRelationship(Arrays.<T>asList(entity), relationshipDefinition, relationshipLoadLevel);
        return true;
    }

    @Override
    public <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> List<REL_ENTRY_CLASS> listRelationshipEntries(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, Key entityId)  {
        GDSBasePersistableObjectManagerBean relationshipManager = getRelationshipManager(relationshipDefinition.getRelationshipEntryDescriptor());
        return relationshipManager.listByField(relationshipDefinition.getRelationshipEntrySourceEntityFieldDescriptor(), entityId);
    }

    @Override
    public <TARGET_CLASS extends BaseEntity> List<TARGET_CLASS> listRelatedEntities(EntityRelationshipDefinition<T, TARGET_CLASS, ?> relationshipDefinition, Key entityId)  {
        switch (relationshipDefinition.getType()) {
            case ONE_TO_MANY:
                return listOneToManyRelatedEntities(relationshipDefinition, entityId);
            case MANY_TO_MANY:
                return listManyToManyRelatedEntities(relationshipDefinition, entityId);
            case ONE_TO_ONE:
                //todo:
        }
        return Collections.emptyList();
    }

    private <TARGET_CLASS extends BaseEntity> List<TARGET_CLASS> listManyToManyRelatedEntities(EntityRelationshipDefinition<T, TARGET_CLASS, ?> relationshipDef, Key entityId)  {
        DatastoreService datastoreService = getDatastoreService();
        GDSBasePersistableObjectManagerBean<? extends BasePersistableRelationshipEntry> relationshipEntryManager = getRelationshipManager(relationshipDef.getRelationshipEntryDescriptor());
        PreparedQuery preparedQuery = datastoreService.prepare(connectionProvider.getActiveTransaction(), new Query(relationshipDef.getTargetClassType().getSimpleName(), KeyFactory.stringToKey(entityId.toString())));
        QueryResultList<Entity> entities = preparedQuery.asQueryResultList(FetchOptions.Builder.withDefaults());
        List<TARGET_CLASS> targetEntities = new ArrayList<TARGET_CLASS>(entities.size());
        for (Entity currEntityKey : entities){

            //todo:bulk get by ids
            BasePersistableRelationshipEntry entry = relationshipEntryManager.getPersistObjectAdapter(null).convert(currEntityKey);
            targetEntities.add(GDSManagerFactory.getManager(relationshipDef.getTargetClassType()).getById(entry.getTargetEntityId()));

        }
        return targetEntities;

    }

    private <TARGET_CLASS extends BaseEntity> List<TARGET_CLASS> listOneToManyRelatedEntities(EntityRelationshipDefinition<T, TARGET_CLASS, ?> relationshipDefinition, Key entityId)  {
        //todo:get only the relationship field rather then the full object...
        T byId = getById(entityId);

        Key targetEntityId = ((EntityOneToManyRelationship) byId.getRelationship(relationshipDefinition)).getTargetEntityId();
        if (targetEntityId == null){
            return Collections.emptyList();
        }else {
            return  Arrays.<TARGET_CLASS>asList(
                    GDSManagerFactory.<TARGET_CLASS>getManager(relationshipDefinition.getTargetClassType(), connectionProvider).getById(targetEntityId));
        }
    }

    @Override
    public List<T> listByRelationShip(EntityRelationshipDefinition relationshipDefinition, Key relatedEntityId)  {
        return list(new QueryFilter(new RelationshipFilterCondition(relationshipDefinition, relatedEntityId)));
    }


    @Override
    public <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> void updateRelationship(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, Key entityId, REL_ENTRY_CLASS relationshipEntry) throws PersistenceException{
        switch (relationshipDefinition.getType()) {
            case ONE_TO_MANY:
                com.google.appengine.api.datastore.Key keyValue = null;
                com.shpandrak.datamodel.field.Key targetEntityId = relationshipEntry.getTargetEntityId();
                if (targetEntityId != null){
                    keyValue = KeyFactory.stringToKey(targetEntityId.toString());
                }
                DatastoreService datastore = getDatastoreService();
                Entity entity;
                try {
                    entity = datastore.get(KeyFactory.stringToKey(entityId.toString()));
                } catch (EntityNotFoundException e) {
                    throw new PersistenceException("Entity with id " + entityId.toString() + " was not found", e);
                }
                entity.setProperty(relationshipDefinition.getName() + "Key", keyValue);
                datastore.put(connectionProvider.getActiveTransaction(), entity);

                break;
            case MANY_TO_MANY:
                IPersistableObjectManager relationshipEntryManager = GDSManagerFactory.getManager(((BasePersistentObjectDescriptor)relationshipDefinition.getRelationshipEntryDescriptor()).getEntityClass(), connectionProvider);
                relationshipEntryManager.create((IPersistableShpandrakObject) relationshipEntry);
                break;
        }
    }
}
