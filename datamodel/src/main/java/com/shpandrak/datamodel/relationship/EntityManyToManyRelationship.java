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
//todo: support different relationship unique key...
public class EntityManyToManyRelationship<SOURCE_CLASS extends BaseEntity, TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends BasePersistableRelationshipEntry<TARGET_CLASS>> extends EntityRelationship<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> {
    //todo: to support many to many relationship with relationship attributes - we'll need a different key...

    private Map<Key, REL_ENTRY_CLASS> relationshipEntryByTargetEntityId = new HashMap<Key, REL_ENTRY_CLASS>();

    @Override
    public boolean isSatisfied() {
        switch (getLoadLevel()){
            case NONE:
                return false;
            default:
                // If at least one map has values - we are satisfied...
                return !relationshipEntryByTargetEntityId.isEmpty();
        }
    }

    @Override
    public void prepareForPersisting() {
        //todo:remove this ugly ugly patch
    }

    public EntityManyToManyRelationship(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition, SOURCE_CLASS ownerEntity) {
        super(definition, ownerEntity);
    }

    @Override
    public void setOwnerEntityId(Key ownerEntityId) {
        for (BasePersistableRelationshipEntry<TARGET_CLASS> currEntry : relationshipEntryByTargetEntityId.values()){
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

    public Map<Key, TARGET_CLASS> getTargetEntities() {
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


//    public void addNewRelatedEntityIds(Collection<UUID> targetEntityIds) {
//        setLoadLevel(RelationshipLoadLevel.ID);
//        for (UUID currTargetEntityId : targetEntityIds){
//            addNewRelatedEntityId(currTargetEntityId);
//        }
//    }

    public void addNewRelation(REL_ENTRY_CLASS relationshipEntry){
        if (relationshipEntry == null) throw new IllegalStateException("Invalid relationship entry - null");
        if (relationshipEntry.getTargetEntityId() == null) throw new IllegalStateException("Invalid target entity id - null");

        relationshipEntry.setSourceEntityId(getOwnerEntity().getId());

        this.relationshipEntryByTargetEntityId.put(relationshipEntry.getTargetEntityId(), relationshipEntry);
        if (relationshipEntry.getTargetEntity() == null){
            setLoadLevelForce(RelationshipLoadLevel.ID);
        }
        else if (getLoadLevel() == RelationshipLoadLevel.NONE){
            setLoadLevel(RelationshipLoadLevel.FULL);
        }
    }

//    public void addNewRelatedEntityId(UUID targetEntityId) {
//        setLoadLevelForce(RelationshipLoadLevel.ID);
//        BasePersistableRelationshipEntry<TARGET_CLASS> entry = this.relationshipEntryByTargetEntityId.get(targetEntityId);
//        // if already loaded don't unload...
//        if (entry == null){
//            BasePersistableRelationshipEntry<TARGET_CLASS> relationshipEntry = (BasePersistableRelationshipEntry<TARGET_CLASS>) getDefinition().getRelationshipEntryDescriptor().instance();
//            relationshipEntry.setSourceEntityId(getOwnerEntity().getId());
//            relationshipEntry.setTargetEntityId(targetEntityId);
//            this.relationshipEntryByTargetEntityId.put(targetEntityId, relationshipEntry);
//        }
//
//    }
//
//    public void addNewRelatedEntities(Collection<TARGET_CLASS> targetEntities) {
//        setLoadLevel(RelationshipLoadLevel.FULL);
//        for (TARGET_CLASS currTargetEntity : targetEntities) {
//            addNewRelatedEntity(currTargetEntity);
//        }
//    }
//    public void addNewRelatedEntity(TARGET_CLASS targetEntity) {
//        setLoadLevel(RelationshipLoadLevel.FULL);
//
//        if (targetEntity == null){
//            throw new IllegalArgumentException("Invalid target entity: null");
//        }
//        //todo:need to support that :(
//        if (targetEntity.getId() == null){
//            throw new IllegalStateException("cannot add related entity that is not persisted (no id). entity " + targetEntity.toString() + " for relationship " + getDefinition().toString());
//        }
//        BasePersistableRelationshipEntry<TARGET_CLASS> relationshipEntry = (BasePersistableRelationshipEntry<TARGET_CLASS>) getDefinition().getRelationshipEntryDescriptor().instance();
//        relationshipEntry.setSourceEntityId(getOwnerEntity().getId());
//        relationshipEntry.setTargetEntity(targetEntity);
//
//        relationshipEntryByTargetEntityId.put(targetEntity.getId(), relationshipEntry);
//    }

    public void set(RelationshipLoadLevel loadLevel, Collection<BasePersistableRelationshipEntry<TARGET_CLASS>> currRelatedEntities){
        setLoadLevel(loadLevel);
        for (BasePersistableRelationshipEntry<TARGET_CLASS> currEntry : currRelatedEntities) {
            relationshipEntryByTargetEntityId.put(currEntry.getTargetEntityId(), (REL_ENTRY_CLASS) currEntry);
        }
    }



}
