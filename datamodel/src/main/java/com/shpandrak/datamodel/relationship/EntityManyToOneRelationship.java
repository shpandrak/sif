package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.field.Key;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/23/12
 * Time: 15:57
 */
public class EntityManyToOneRelationship<SOURCE_CLASS extends BaseEntity, TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<TARGET_CLASS>> extends EntityRelationship<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> {
    private Map<Key, REL_ENTRY_CLASS> relationshipEntryByTargetEntityId = new HashMap<Key, REL_ENTRY_CLASS>();
    private List<REL_ENTRY_CLASS> orderedRelationshipEntries = new ArrayList<REL_ENTRY_CLASS>();

    @Override
    public boolean isSatisfied() {
        switch (getLoadLevel()){
            case NONE:
                return false;
            default:
                // If at least one map has values - we are satisfied...
                //todo:not good. null check? introduce status field?
                return !relationshipEntryByTargetEntityId.isEmpty();
        }
    }

    @Override
    public void prepareForPersisting() {
        //todo:remove stupid ugly patch method that this is
    }

    public EntityManyToOneRelationship(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition, SOURCE_CLASS ownerEntity) {
        super(definition, ownerEntity);
    }

    @Override
    public void setOwnerEntityId(Key ownerEntityId) {
        for (REL_ENTRY_CLASS currEntry : relationshipEntryByTargetEntityId.values()){
            currEntry.setSourceEntityId(ownerEntityId);
        }
    }

    public Set<Key> getTargetEntityIds() {
        switch (getLoadLevel()){
            case FULL:
            case ID:
                return relationshipEntryByTargetEntityId.keySet();
            default:
                throw new IllegalStateException("Relationship Ids were not loaded. for relationship " + getDefinition().toString());
        }
    }

    public Map<Key, REL_ENTRY_CLASS> getRelationshipEntriesByTargetEntityId() {
        switch (getLoadLevel()){
            case FULL:
            case ID:
                return relationshipEntryByTargetEntityId;
            default:
                throw new IllegalStateException("Relationship was not loaded. for relationship " + getDefinition().toString());
        }
    }

    public List<REL_ENTRY_CLASS> getOrderedRelationshipEntries() {
        switch (getLoadLevel()){
            case FULL:
            case ID:
                return orderedRelationshipEntries;
            default:
                throw new IllegalStateException("Relationship was not loaded. for relationship " + getDefinition().toString());
        }
    }

    public List<TARGET_CLASS> getTargetEntities() {
        switch (getLoadLevel()){
            case FULL:
                List<TARGET_CLASS> targetEntities = new ArrayList<TARGET_CLASS>(orderedRelationshipEntries.size());
                for (REL_ENTRY_CLASS currEntry : orderedRelationshipEntries){
                    targetEntities.add(currEntry.getTargetEntity());
                }
                return targetEntities;
            case ID:
                throw new IllegalStateException("Relationship entities were not loaded in Full level (Id only) for relationship " + getDefinition().toString());
            default:
                throw new IllegalStateException("Relationship was not loaded. for relationship " + getDefinition().toString());
        }

    }
    public Map<Key, TARGET_CLASS> getTargetEntitiesMap() {
        switch (getLoadLevel()){
            case FULL:
                Map<Key, TARGET_CLASS> targetEntities = new HashMap<Key, TARGET_CLASS>(relationshipEntryByTargetEntityId.size());
                for (Map.Entry<Key, REL_ENTRY_CLASS> currEntry : relationshipEntryByTargetEntityId.entrySet()){
                    targetEntities.put(currEntry.getKey(), currEntry.getValue().getTargetEntity());
                }
                return targetEntities;
            case ID:
                throw new IllegalStateException("Relationship entities were not loaded in Full level (Id only) for relationship " + getDefinition().toString());
            default:
                throw new IllegalStateException("Relationship was not loaded. for relationship " + getDefinition().toString());
        }
    }


    public void addNewRelation(REL_ENTRY_CLASS relationshipEntry){
        if (relationshipEntry == null) throw new IllegalStateException("Invalid relationship entry - null");
        if (relationshipEntry.getTargetEntityId() == null) throw new IllegalStateException("Invalid target entity id - null");

        relationshipEntry.setSourceEntityId(getOwnerEntity().getId());

        this.relationshipEntryByTargetEntityId.put(relationshipEntry.getTargetEntityId(), relationshipEntry);
        this.orderedRelationshipEntries.add(relationshipEntry);
        if (relationshipEntry.getTargetEntity() == null){
            setLoadLevelForce(RelationshipLoadLevel.ID);
        }
        else if (getLoadLevel() == RelationshipLoadLevel.NONE){
            setLoadLevel(RelationshipLoadLevel.FULL);
        }
    }

    public void setFull(Collection<TARGET_CLASS> targetEntities){
        setLoadLevel(RelationshipLoadLevel.FULL);
        for (TARGET_CLASS currEntity : targetEntities) {
            REL_ENTRY_CLASS targetEntry = (REL_ENTRY_CLASS) getDefinition().getRelationshipEntryDescriptor().instance();
            targetEntry.setTargetEntity(currEntity);
            targetEntry.setSourceEntityId(getOwnerEntity().getId());
            relationshipEntryByTargetEntityId.put(currEntity.getId(), targetEntry);
            orderedRelationshipEntries.add(targetEntry);
        }
    }

    public void setIds(Collection<Key> targetEntityIds){
        setLoadLevel(RelationshipLoadLevel.ID);
        for (Key currTargetEntityId : targetEntityIds) {
            REL_ENTRY_CLASS targetEntry = (REL_ENTRY_CLASS) getDefinition().getRelationshipEntryDescriptor().instance();
            targetEntry.setTargetEntityId(currTargetEntityId);
            targetEntry.setSourceEntityId(getOwnerEntity().getId());
            relationshipEntryByTargetEntityId.put(currTargetEntityId, targetEntry);
            orderedRelationshipEntries.add(targetEntry);
        }
    }



}
