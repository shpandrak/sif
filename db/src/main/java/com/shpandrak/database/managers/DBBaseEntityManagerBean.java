package com.shpandrak.database.managers;

import com.shpandrak.database.DBException;
import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.table.DBEmbeddedRelationshipKeyField;
import com.shpandrak.database.table.DBRelationshipTable;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.database.table.relationship.RelationshipEntryQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.*;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.relationship.*;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.PersistenceLayerManager;
import com.shpandrak.persistence.managers.IEntityManager;
import com.shpandrak.persistence.managers.EntityRelationshipValidator;
import com.shpandrak.persistence.managers.IPersistableObjectManager;
import com.shpandrak.persistence.query.filter.QueryFilter;
import com.shpandrak.persistence.query.filter.RelationshipFilterCondition;
import com.shpandrak.persistence.query.filter.RelationshipLoadInfo;
import com.shpandrak.persistence.query.filter.RelationshipLoadInstructions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 23:00
 */
public abstract class DBBaseEntityManagerBean<T extends BaseEntity> extends DBBasePersistableObjectManagerBean<T> implements IEntityManager<T> {
    private static final Logger logger = LoggerFactory.getLogger(DBBaseReadOnlyManagerBean.class);

    @Override
    public List<T> list(QueryFilter filter, RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException {
        //todo: merge loadLevel into queryFilter?
        return list(filter);
    }

    @Override
    public T findObject(QueryFilter filter, RelationshipLoadInstructions relationshipLoadInstructions) throws PersistenceException {
        //todo: load the relations!!!!!!
        return findObject(filter);
    }

    @Override
    protected PersistableObjectAdapter<T> getPersistEntityAdapter(String tableAlias) {
        return new EntityAdapter<T>(getDbTable(), tableAlias, getEntityDescriptor());
    }


    @Override
    public T getById(Key id, RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException {
        return getById(id, getRelationshipLoadInstructions(relationshipLoadLevel));
    }

    @Override
    public <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> List<REL_ENTRY_CLASS> listRelationshipEntries(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, Key entityId) throws PersistenceException {
        DBBasePersistableObjectManagerBean relationshipManager = getRelationshipManager(relationshipDefinition.getRelationshipEntryDescriptor());
        return relationshipManager.listByField(relationshipDefinition.getRelationshipEntrySourceEntityFieldDescriptor(), entityId);
    }

    @Override
    public boolean loadRelationship(T entity, EntityRelationshipDefinition relationshipDefinition, RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException {
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

        loadRelationship(Arrays.asList(entity), relationshipDefinition, relationshipLoadLevel, null, null);
        return true;
    }

    @Override
    public <TARGET_CLASS extends BaseEntity> List<TARGET_CLASS> listRelatedEntities(EntityRelationshipDefinition<T, TARGET_CLASS, ?> relationshipDefinition, Key entityId) throws PersistenceException {
        switch (relationshipDefinition.getType()) {
            case ONE_TO_MANY:
                return listOneToManyRelatedEntities(relationshipDefinition, entityId);
            case MANY_TO_MANY:
                return listManyToManyRelatedEntities(relationshipDefinition, entityId, null, null);
            case ONE_TO_ONE:
                //todo:
        }
        return Collections.emptyList();
    }


    private <TARGET_CLASS extends BaseEntity> List<TARGET_CLASS> listOneToManyRelatedEntities(EntityRelationshipDefinition<T, TARGET_CLASS, ?> relationshipDef, Key entityId) throws PersistenceException {
        Class<TARGET_CLASS> targetClass = relationshipDef.getTargetClassType();
        DBBaseEntityManagerBean<TARGET_CLASS> targetClassManager = (DBBaseEntityManagerBean<TARGET_CLASS>) DBManagerFactory.getManager(targetClass, connectionProvider);
        DBTable targetTable = targetClassManager.dbTable;
        DBEmbeddedRelationshipKeyField embeddedRelationshipIKeyField = dbTable.getEmbeddedRelationshipField(relationshipDef);

        String originalEntitySQL = "select " + embeddedRelationshipIKeyField.getPersistentFieldName() + " " +
                "from " + dbTable.getName() + " " +
                "where id = ?";

        String relatedEntitySQL =
                "select " + targetTable.getAllFieldsAsString("t") + " " +
                        "from " + targetTable.getName() + " t, " +
                        "(" + originalEntitySQL + ") org " +
                        "where t.id = org." + embeddedRelationshipIKeyField.getPersistentFieldName();

        return targetClassManager.listBySql(relatedEntitySQL, DBUtil.keyAsParams(entityId), "t", null);
    }

    private <TARGET_CLASS extends BaseEntity> List<TARGET_CLASS> listManyToManyRelatedEntities(EntityRelationshipDefinition<T, TARGET_CLASS, ?> relationshipDef, Key entityId, String originalEntitySQL, List<Object> originalEntitySQLParams) throws PersistenceException {
        Class<TARGET_CLASS> targetClass = relationshipDef.getTargetClassType();
        DBBaseReadOnlyManagerBean<TARGET_CLASS> targetClassManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(targetClass, connectionProvider);
        DBBaseReadOnlyManagerBean relationshipEntryManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(((BasePersistentObjectDescriptor)relationshipDef.getRelationshipEntryDescriptor()).getEntityClass(), connectionProvider);

        DBRelationshipTable relationshipTable = (DBRelationshipTable) relationshipEntryManager.getDbTable();
        DBTable targetTable = targetClassManager.getDbTable();

        String relatedEntitySQL;
        List<Object> relationshipQueryParams;

        relationshipQueryParams = DBUtil.keyAsParams(entityId);
        relatedEntitySQL =
                "select distinct " + targetTable.getAllFieldsAsString("t") + " " +
                        "from " +
                        targetTable.getName() + " t, " +
                        relationshipTable.getName() + " rel " +
                        "where " +
                        "t.id = rel." + relationshipTable.getTargetField().getPersistentFieldName() + " and " +
                        "rel." + relationshipTable.getSourceField().getPersistentFieldName() + " = ?";

        return getQueryService().getList(relatedEntitySQL, targetClassManager.getQueryConverter("t"), relationshipQueryParams);
    }


    @Override
    public T getById(Key id, RelationshipLoadInstructions loadInstructions) throws PersistenceException {
        String sql = dbTable.getAllFieldsSQL() + " where id = ?";
        List<Object> params = DBUtil.keyAsParams(id);
        T entity = getQueryService().getSingleValue(sql, getQueryConverter(), params);
        if (entity == null) return null;

        loadRelationships(loadInstructions, Arrays.<T>asList(entity), sql, params);


        return entity;
    }


    @Override
    public List<T> list(RelationshipLoadLevel relationshipLoadLevel) throws PersistenceException {
        String sql = dbTable.getAllFieldsSQL();
        List<Object> params = null;
        RelationshipLoadInstructions loadInstructions = getRelationshipLoadInstructions(relationshipLoadLevel);

        return listBySql(sql, params, null, loadInstructions);
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

    private List<T> listBySql(String sql, List<Object> params, String tableAlias, RelationshipLoadInstructions relationshipLoadLevelMap) throws PersistenceException {
        List<T> list = getQueryService().getList(sql, getQueryConverter(tableAlias), params);
        if (list.isEmpty()) return list;

        loadRelationships(relationshipLoadLevelMap, list, sql, params);

        return list;
    }

    private void loadRelationships(RelationshipLoadInstructions loadInstructions, List<T> list, String originalEntitySQL, List<Object> originalEntitySQLParams) throws PersistenceException {
        if (loadInstructions != null) {
            for (RelationshipLoadInfo currRelationshipDefEntry : loadInstructions.getLoadInfo()) {
                EntityRelationshipDefinition relationshipDef = currRelationshipDefEntry.getRelationshipDefinition();
                loadRelationship(list, relationshipDef, currRelationshipDefEntry.getLoadLevel(), originalEntitySQL, originalEntitySQLParams);
            }
        }
    }

    private void loadRelationship(List<T> list, EntityRelationshipDefinition relationshipDefinition, RelationshipLoadLevel relationshipLoadLevel, String originalEntitySQL, List<Object> originalEntitySQLParams) throws PersistenceException {
        switch (relationshipLoadLevel) {
            case NONE:
                break;
            case ID:
                switch (relationshipDefinition.getType()) {
                    case ONE_TO_MANY:
                        // Id gets loaded for free...
                        break;
                    case MANY_TO_MANY:
                        loadManyToManyRelationshipWithIdLevel(originalEntitySQL, originalEntitySQLParams, relationshipDefinition, list);
                        break;
                    case MANY_TO_ONE:
                        loadManyToOneRelationshipWithIdLevel(relationshipDefinition, list, originalEntitySQL, originalEntitySQLParams);
                        break;

                    case ONE_TO_ONE:
                        //todo:
                }
                break;
            case FULL:

                switch (relationshipDefinition.getType()) {
                    case ONE_TO_MANY:
                        loadOneToManyRelationship(originalEntitySQL, originalEntitySQLParams, relationshipDefinition, list);
                        break;
                    case MANY_TO_MANY:
                        loadManyToManyRelationshipFullLevel(originalEntitySQL, originalEntitySQLParams, relationshipDefinition, list);
                        break;
                    case MANY_TO_ONE:
                        loadManyToOneRelationshipWithFullLevel(relationshipDefinition, list, originalEntitySQL, originalEntitySQLParams);
                    case ONE_TO_ONE:
                        //todo:
                }
                break;

        }
    }

    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<TARGET_CLASS>> void loadManyToOneRelationshipWithFullLevel(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, List<T> list, String originalEntitySQL, List<Object> originalEntitySQLParams) throws DBException {
        EntityRelationshipDefinition reverseRelationshipDefinition = relationshipDefinition.getReverseRelationshipDefinition();
        if (reverseRelationshipDefinition == null){
            throw new IllegalStateException("Only two-side relationship allowed for many to one relationship (todo)");
            //todo:support that...
        }


        DBBaseReadOnlyManagerBean<TARGET_CLASS> targetClassManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(reverseRelationshipDefinition.getSourceClassType(), connectionProvider);

        // todo:bulk loading!!!!
        for (T currEntity : list){
            List<TARGET_CLASS> targetEntities = targetClassManager.list(new QueryFilter(new RelationshipFilterCondition(reverseRelationshipDefinition, currEntity.getId())));
            ((EntityManyToOneRelationship<T, TARGET_CLASS, REL_ENTRY_CLASS>) currEntity.getRelationship(relationshipDefinition)).setFull(targetEntities);
        }
    }
    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<TARGET_CLASS>> void loadManyToOneRelationshipWithIdLevel(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, List<T> list, String originalEntitySQL, List<Object> originalEntitySQLParams) throws DBException {
        EntityRelationshipDefinition reverseRelationshipDefinition = relationshipDefinition.getReverseRelationshipDefinition();
        if (reverseRelationshipDefinition == null){
            throw new IllegalStateException("Only two-side relationship allowed for many to one relationship (todo)");
            //todo:support that...
        }

        DBBaseReadOnlyManagerBean<TARGET_CLASS> targetClassManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(reverseRelationshipDefinition.getSourceClassType(), connectionProvider);

        // todo:bulk loading!!!!
        for (T currEntity : list){
            Set<Key> targetIds = targetClassManager.listIds(new QueryFilter(new RelationshipFilterCondition(reverseRelationshipDefinition, currEntity.getId())));
            ((EntityManyToOneRelationship<T, TARGET_CLASS, REL_ENTRY_CLASS>) currEntity.getRelationship(relationshipDefinition)).setIds(targetIds);
        }
    }

    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<TARGET_CLASS>> void loadOneToManyRelationship(String originalEntitySQL, List<Object> params, EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDef, List<T> list) throws PersistenceException {
        Class<TARGET_CLASS> targetClass = relationshipDef.getTargetClassType();
        //todo:future: support relationship to different persistent layer implementation don't cast here and encapsulate better
        DBBaseReadOnlyManagerBean<TARGET_CLASS> targetClassManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(targetClass, connectionProvider);

        //todo:in the inner sql fetch only the dbOneToManyRelationship.getSourceField() and not all fields...
        DBTable targetTable = targetClassManager.dbTable;
        DBEmbeddedRelationshipKeyField embeddedRelationshipUUIDField = dbTable.getEmbeddedRelationshipField(relationshipDef);


        if (list.size() == 1) {

            // If we have only one entity and we're here fetching a one-to-many relationship - this is actually doing a getById
            // because we load the relatedEntityId by default (it is in the originalEntity table...)
            T orgEntity = list.get(0);
            Key targetEntityId = orgEntity.getOneToManyRelationship(relationshipDef).getTargetEntityId();
            if (targetEntityId == null) {
                throw new PersistenceException("Invalid original entity of type " + orgEntity.getClass().getSimpleName() + " loaded with no id for one-to-many relationship " + relationshipDef.toString());
            }
            String relatedEntitySQL =
                    targetTable.getAllFieldsSQL() + " where id = ?";

            loadOneToManyFullLevelBySQL(relationshipDef, list, targetClassManager, relatedEntitySQL, DBUtil.keyAsParams(targetEntityId), null);

        } else if (originalEntitySQL == null){
            //todo:bulk loading here please instead of this shit...
            for (T currEntity : list){
                loadOneToManyRelationship(originalEntitySQL, params, relationshipDef, Arrays.asList(currEntity));
            }

        }

        else {
            String targetTableAlias = "t";
            String relatedEntitySQL =
                    "select " + targetTable.getAllFieldsAsString("t") + " " +
                            "from " + targetTable.getName() + " t, " +
                            "(" + originalEntitySQL + ") org " +
                            "where t.id = org." + embeddedRelationshipUUIDField.getPersistentFieldName();

            loadOneToManyFullLevelBySQL(relationshipDef, list, targetClassManager, relatedEntitySQL, params, targetTableAlias);
        }

    }

    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<TARGET_CLASS>> void loadOneToManyFullLevelBySQL(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDef, List<T> list, DBBaseReadOnlyManagerBean<TARGET_CLASS> targetClassManager, String relatedEntitySQL, List<Object> relationshipQueryParams, String targetTableAlias) throws PersistenceException {
        Map<Key, TARGET_CLASS> relatedEntitiesById = targetClassManager.getMapByIdForSQL(relatedEntitySQL, relationshipQueryParams, targetTableAlias);
        for (T currEntity : list) {
            EntityOneToManyRelationship<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipInstance = currEntity.getOneToManyRelationship(relationshipDef);
            Key relatedEntityId = relationshipInstance.getTargetEntityId();
            if (relatedEntityId != null) {
                TARGET_CLASS loadedRelatedEntity = relatedEntitiesById.get(relatedEntityId);
                if (loadedRelatedEntity == null) {
                    throw new PersistenceException("Unable to satisfy relationship " + relationshipDef + " for target entity with id " + relatedEntityId);
                }
                relationshipInstance.setTargetEntity(loadedRelatedEntity);
            }
        }
    }

    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends BasePersistableRelationshipEntry<TARGET_CLASS>> void loadManyToManyRelationshipWithIdLevel(String originalEntitySQL, List<Object> originalEntitySQLParams, EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDef, List<T> list) throws PersistenceException {
        //todo:future: support relationship to different persistent layer implementation don't cast here and encapsulate better
        //todo:in the inner sql fetch only the dbOneToManyRelationship.getSourceField() and not all fields...
        DBBasePersistableObjectManagerBean relationshipEntryManager = getRelationshipManager(relationshipDef.getRelationshipEntryDescriptor());
        DBRelationshipTable relationshipTable = (DBRelationshipTable) relationshipEntryManager.getDbTable();

        if (list.size() == 1) {
            T orgEntity = list.get(0);
            if (orgEntity.getId() == null) {
                throw new PersistenceException("Failed loading relationship " + relationshipDef.getName() + " : Invalid original entity of type " + orgEntity.getClass().getSimpleName() + " loaded with id null");
            }

            String relatedEntitySQL =
                    relationshipTable.getAllFieldsSQL("rel") + " " +
                            "where " +
                            "rel." + relationshipTable.getSourceField().getPersistentFieldName() + " = ?";

            loadManyToManyRelationshipsBySQL(relationshipDef, list, relationshipTable, relatedEntitySQL, DBUtil.keyAsParams(orgEntity.getId()));

        } else if (originalEntitySQL == null){
            //todo: more efficient bulk loading in case we don't have the SQL e.g. temp table or at least split to in clauses...
            for (T currEntity : list){
                loadManyToManyRelationshipWithIdLevel(originalEntitySQL, originalEntitySQLParams, relationshipDef, Arrays.asList(currEntity));
            }


        }else{
            String relatedEntitySQL =
                    relationshipTable.getAllFieldsSQL("rel") +
                            ", " +
                            "(" + originalEntitySQL + ") org " +
                            "where " +
                            "rel." + relationshipTable.getSourceField().getPersistentFieldName() + " = org.id";

            loadManyToManyRelationshipsBySQL(relationshipDef, list, relationshipTable, relatedEntitySQL, originalEntitySQLParams);
        }




    }



    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends BasePersistableRelationshipEntry<TARGET_CLASS>> void loadManyToManyRelationshipsBySQL(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDef, List<T> list, DBRelationshipTable relationshipTable, String relatedEntitySQL, List<Object> relationshipQueryParams) throws DBException {

        Map<Key, List<BasePersistableRelationshipEntry<TARGET_CLASS>>> targetEntitiesByCurrentEntityId = getQueryService().getMapOfLists(relatedEntitySQL, relationshipTable.getSourceField().getQueryConverter("rel"), new RelationshipEntryQueryConverter<TARGET_CLASS>(relationshipTable, "rel", relationshipDef.getRelationshipEntryDescriptor()), relationshipQueryParams);
        for (T currEntity : list) {
            EntityManyToManyRelationship<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipInstance = currEntity.getManyToManyRelationship(relationshipDef);
            List<BasePersistableRelationshipEntry<TARGET_CLASS>> currRelatedEntities = targetEntitiesByCurrentEntityId.get(currEntity.getId());
            if (currRelatedEntities == null) {
                currRelatedEntities = Collections.emptyList();
            }
            relationshipInstance.set(RelationshipLoadLevel.ID, currRelatedEntities);
        }
    }

    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends BasePersistableRelationshipEntry<TARGET_CLASS>> void loadManyToManyRelationshipFullLevel(String originalEntitySQL, List<Object> params, EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDef, List<T> list) throws PersistenceException {
        Class<TARGET_CLASS> targetClass = relationshipDef.getTargetClassType();
        //todo:future: support relationship to different persistent layer implementation don't cast here and encapsulate better
        DBBaseReadOnlyManagerBean<TARGET_CLASS> targetClassManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(targetClass, connectionProvider);
        DBBaseReadOnlyManagerBean<REL_ENTRY_CLASS> relationshipEntryManager = (DBBaseReadOnlyManagerBean) DBManagerFactory.getManager(((BasePersistentObjectDescriptor)relationshipDef.getRelationshipEntryDescriptor()).getEntityClass(), connectionProvider);


        //todo:in the inner sql fetch only the dbOneToManyRelationship.getSourceField() and not all fields...
        DBRelationshipTable relationshipTable = (DBRelationshipTable) relationshipEntryManager.getDbTable();
        DBTable targetTable = targetClassManager.getDbTable();

        if (list.size() == 1) {
            T orgEntity = list.get(0);
            if (orgEntity.getId() == null) {
                throw new PersistenceException("Failed loading relationship " + relationshipDef.getName() + " : Invalid original entity of type " + orgEntity.getClass().getSimpleName() + " loaded with id null");
            }
            String relatedEntitySQL =
                    "select " + relationshipTable.getAllFieldsAsString("rel") + ", " + targetTable.getAllFieldsAsString("t") + " " +
                            "from " +
                            targetTable.getName() + " t, " +
                            relationshipTable.getName() + " rel " +
                            "where " +
                            "t.id = rel." + relationshipTable.getTargetField().getPersistentFieldName() + " and " +
                            "rel." + relationshipTable.getSourceField().getPersistentFieldName() + " = ?";

            loadManyToManyFullBySQL(relationshipDef, list, targetClassManager, relationshipTable, relatedEntitySQL, DBUtil.keyAsParams(orgEntity.getId()));



        } else if (originalEntitySQL == null){
            //todo: bulk loading instead of this inefficient shit
            for (T currEntity : list){
                loadManyToManyRelationshipFullLevel(originalEntitySQL, params, relationshipDef, Arrays.asList(currEntity));
            }

        }else {
            String relatedEntitySQL =
                    "select " + relationshipTable.getAllFieldsAsString("rel") + ", " + targetTable.getAllFieldsAsString("t") + " " +
                            "from " +
                            targetTable.getName() + " t, " +
                            relationshipTable.getName() + " rel, " +
                            "(" + originalEntitySQL + ") org " +
                            "where " +
                            "t.id = rel." + relationshipTable.getTargetField().getPersistentFieldName() + " and " +
                            "rel." + relationshipTable.getSourceField().getPersistentFieldName() + " = org.id";
            loadManyToManyFullBySQL(relationshipDef, list, targetClassManager, relationshipTable, relatedEntitySQL, params);
        }

    }

    private <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends BasePersistableRelationshipEntry<TARGET_CLASS>> void loadManyToManyFullBySQL(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDef, List<T> list, DBBaseReadOnlyManagerBean<TARGET_CLASS> targetClassManager, DBRelationshipTable relationshipTable, String relatedEntitySQL, List<Object> relationshipQueryParams) throws DBException {
        Map<Key, List<BasePersistableRelationshipEntry<TARGET_CLASS>>> targetEntitiesByCurrentEntityId = getQueryService().getMapOfLists(relatedEntitySQL, relationshipTable.getSourceField().getQueryConverter ("rel"), new RelationshipEntryQueryConverter<TARGET_CLASS>(relationshipTable, "rel", relationshipDef.getRelationshipEntryDescriptor(), targetClassManager.getQueryConverter("t")), relationshipQueryParams);

        for (T currEntity : list) {
            EntityManyToManyRelationship<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipInstance = currEntity.getManyToManyRelationship(relationshipDef);
            List<BasePersistableRelationshipEntry<TARGET_CLASS>> currRelatedEntities = targetEntitiesByCurrentEntityId.get(currEntity.getId());
            if (currRelatedEntities == null) {
                currRelatedEntities = Collections.emptyList();
            }
            relationshipInstance.set(RelationshipLoadLevel.FULL, currRelatedEntities);
        }
    }



    protected DBBaseEntityManagerBean(DBTable dbTable, IDBConnectionProvider connectionProvider) {
        super(dbTable, connectionProvider);
    }

    protected DBBaseEntityManagerBean(DBTable dbTable) throws PersistenceException {
        super(dbTable, (IDBConnectionProvider) PersistenceLayerManager.getConnectionProvider());
    }

    @Override
    public T create(T entity) throws PersistenceException {
        // Validating Entity relationships
        EntityRelationshipValidator.validateRelationshipsForCreate(entity);

        // Inserting to db
        T createdEntity = super.create(entity);

        // Persisting relationships
        createRelationships(createdEntity);

        return createdEntity;
    }

    @Override
    public List<T> create(Collection<T> entities) throws PersistenceException {
        // Validating Entities relationships
        for (T currEntity : entities){
            EntityRelationshipValidator.validateRelationshipsForCreate(currEntity);
        }

        // Bulk saving
        List<T> createdEntities = super.create(entities);

        // Persisting relationships
        //todo: can and must bulk insert here!!!

        for (T currEntity : createdEntities){
            createRelationships(currEntity);
        }

        return createdEntities;

    }

    protected <E extends IPersistableShpandrakObject> void insertEntities(Collection<E> objects, DBTable dbTable, PersistableObjectAdapter<E> adapter) throws DBException {
        if (objects == null || objects.isEmpty()) return;

        ArrayList<List<Object>> params = new ArrayList<List<Object>>(objects.size());
        for (E currObject : objects) {
            adapter.generateKey(currObject);
            params.add(adapter.prepareForPersisting(currObject));
        }

        // Inserting to db
        dbAccessService.executeUpdates(dbTable.getInsertSQL(), params);

    }

    @Override
    public int delete(Collection<Key> objectIds) throws PersistenceException {
        int numRows = 0;
        //todo:performance - do some bulking here!
        for (Key currId : objectIds) {
            boolean delete = delete(currId);
            if (delete) ++numRows;
        }
        return numRows;
    }

    @Override
    public boolean delete(Key entityId) throws PersistenceException {
        if (entityId == null) {
            throw new IllegalArgumentException("Illegal entity id: null, bye now...");
        }

        boolean actuallyDeleted = true;
        BaseEntityDescriptor<T> entityDescriptor = getEntityDescriptor();
        if (entityDescriptor.getRelationshipDefinitionMap().isEmpty()) {
            int rowsDeleted = deleteFromTableById(entityId);
            if (rowsDeleted == 0) {
                actuallyDeleted = false;
                logger.info("tried deleting non-existing entity of type {} with id {}", getEntityClass().getSimpleName(), entityId);
            }
        } else {

            //todo: load only relevant relationships for deletion
            T entity = getById(entityId, RelationshipLoadLevel.ID);
            if (entity == null) {
                logger.info("tried deleting non-existing entity of type {} with id {}", getEntityClass().getSimpleName(), entityId);
                actuallyDeleted = false;
            } else {

                // Deleting the entity relationships
                deleteEntityRelationships(entity);

                // Deleting from actual table
                deleteFromTableById(entityId);
            }
        }

        return actuallyDeleted;
    }

    protected BaseEntityDescriptor<T> getEntityDescriptor() {
        return EntityDescriptorFactory.get(getEntityClass());
    }

    @Override
    protected ShpandrakObjectDescriptor getDescriptor() {
        return getEntityDescriptor();
    }

    private void deleteEntityRelationships(T entity) throws PersistenceException {

        //todo:validate relationships are loaded with id level at least here. yes, I know it is a private method - but will it stay that way in the future?
        List<EntityRelationship> entityRelationships = entity.getLoadedRelationships();
        Map<EntityRelationshipDefinition, List<Key>> relationshipInsertParameters = new HashMap<EntityRelationshipDefinition, List<Key>>(entityRelationships.size());
        for (EntityRelationship currRelationship : entityRelationships) {
            switch (currRelationship.getDefinition().getType()) {
                case MANY_TO_MANY:
                    EntityManyToManyRelationship manyToManyRelationship = (EntityManyToManyRelationship) currRelationship;
                    Map<Key, ? extends BasePersistableRelationshipEntry<? extends BaseEntity>> relationshipEntriesByTargetEntityId = manyToManyRelationship.getRelationshipEntriesByTargetEntityId();
                    if (!relationshipEntriesByTargetEntityId.isEmpty()) {

                        List<Key> relationshipEntryIds = new ArrayList<Key>(relationshipEntriesByTargetEntityId.size());
                        for (Map.Entry<Key, ? extends BasePersistableRelationshipEntry<? extends BaseEntity>> currRelEntry : relationshipEntriesByTargetEntityId.entrySet()) {
                            relationshipEntryIds.add(currRelEntry.getValue().getId());
                        }
                        relationshipInsertParameters.put(currRelationship.getDefinition(), relationshipEntryIds);
                    }

            }
        }

        for (Map.Entry<EntityRelationshipDefinition, List<Key>> currRelationshipEntry : relationshipInsertParameters.entrySet()) {
            switch (currRelationshipEntry.getKey().getType()) {
                case MANY_TO_MANY:
                    IPersistableObjectManager relationshipEntryManager = getRelationshipManager(currRelationshipEntry.getKey().getRelationshipEntryDescriptor());
                    // Getting relationship table from relationship definition
                    relationshipEntryManager.delete(currRelationshipEntry.getValue());
            }
        }
    }

    private DBBasePersistableObjectManagerBean getRelationshipManager(ShpandrakObjectDescriptor relationshipEntryDescriptor) throws PersistenceException {
        return (DBBasePersistableObjectManagerBean) DBManagerFactory.getManager(((BasePersistentObjectDescriptor)relationshipEntryDescriptor).getEntityClass(), connectionProvider);
    }

    private void createRelationships(T entity) throws PersistenceException {

        // For every relationship we gather the sql parameters for bulk insertion
        List<EntityRelationship> entityRelationships = entity.getLoadedRelationships();
        for (EntityRelationship currRelationship : entityRelationships) {
            switch (currRelationship.getDefinition().getType()) {
                case MANY_TO_MANY:
                    EntityManyToManyRelationship manyToManyRelationship = (EntityManyToManyRelationship) currRelationship;
                    IPersistableObjectManager relationshipEntryManager = DBManagerFactory.getManager(((BasePersistentObjectDescriptor)manyToManyRelationship.getDefinition().getRelationshipEntryDescriptor()).getEntityClass(), connectionProvider);
                    relationshipEntryManager.create(manyToManyRelationship.getRelationshipEntriesByTargetEntityId().values());
                    break;
            }
        }

    }


    @Override
    protected IQueryConverter<T> getQueryConverter(String tableAlias) {
        return getPersistEntityAdapter(tableAlias);
    }

    @Override
    public List<T> listByRelationShip(EntityRelationshipDefinition relationshipDefinition, Key relatedEntityId) throws DBException {
        return list(new QueryFilter(new RelationshipFilterCondition(relationshipDefinition, relatedEntityId)));
    }

    @Override
    public <TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> void updateRelationship(EntityRelationshipDefinition<T, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, Key entityId, REL_ENTRY_CLASS relationshipEntry) throws PersistenceException {
        switch (relationshipDefinition.getType()) {
            case ONE_TO_MANY:
                DBEmbeddedRelationshipKeyField embeddedRelationshipUUIDField = dbTable.getEmbeddedRelationshipField(relationshipDefinition);
                dbAccessService.executeUpdate("update " + dbTable.getName() + " set " + embeddedRelationshipUUIDField.getPersistentFieldName() + "=? where id=?", Arrays.<Object>asList(embeddedRelationshipUUIDField.prepareForPersisting(relationshipEntry.getTargetEntityId()), dbTable.getFields().get(0).prepareForPersisting(entityId)));
                break;
            case MANY_TO_MANY:
                IPersistableObjectManager relationshipEntryManager = DBManagerFactory.getManager(((BasePersistentObjectDescriptor)relationshipDefinition.getRelationshipEntryDescriptor()).getEntityClass(), connectionProvider);
                relationshipEntryManager.create((IPersistableShpandrakObject) relationshipEntry);
                break;
        }
    }
}
