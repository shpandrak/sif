package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.field.Key;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/23/12
 * Time: 09:54
 */
public class EntityOneToManyRelationship<SOURCE_CLASS extends BaseEntity, TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<TARGET_CLASS>> extends EntityRelationship<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> {
    private RelationshipEntry<TARGET_CLASS> relationshipEntry = null;

    public EntityOneToManyRelationship(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition, SOURCE_CLASS ownerEntity, Key targetEntityId) {
        super(definition, ownerEntity);
        setTargetEntityId(targetEntityId);
    }

    public EntityOneToManyRelationship(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition, SOURCE_CLASS ownerEntity, TARGET_CLASS targetEntity) {
        super(definition, ownerEntity);
        setTargetEntity(targetEntity);
    }

    @Override
    public boolean isSatisfied() {
        switch (getLoadLevel()){
            case NONE:
                return false;
            default:
                return getTargetEntityId() != null;
        }
    }

    @Override
    public void prepareForPersisting() {
        //todo:remove this ugly patch method
        if (relationshipEntry != null){
            if (relationshipEntry.getTargetEntityId() == null &&
                relationshipEntry.getTargetEntity() != null &&
                relationshipEntry.getTargetEntity().getId() != null){
                relationshipEntry.setTargetEntityId(relationshipEntry.getTargetEntity().getId());
            }
        }

    }

    public RelationshipEntry<TARGET_CLASS> getRelationshipEntry() {
        return relationshipEntry;
    }

    public void setRelationshipEntry(RelationshipEntry<TARGET_CLASS> relationshipEntry) {
        this.relationshipEntry = relationshipEntry;
        if (relationshipEntry.getTargetEntity() == null && relationshipEntry.getTargetEntityId() != null){
            setLoadLevelForce(RelationshipLoadLevel.ID);
        }else {
            setLoadLevelForce(RelationshipLoadLevel.FULL);
        }
    }

    public EntityOneToManyRelationship(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition, SOURCE_CLASS ownerEntity) {
        super(definition, ownerEntity);
    }

    @Override
    public void setOwnerEntityId(Key ownerEntityId) {
        if (relationshipEntry != null){
            relationshipEntry.setSourceEntityId(ownerEntityId);
        }
    }

    public Key getTargetEntityId() {
        switch (getLoadLevel()){
           case FULL:
           case ID:
               return this.relationshipEntry.getTargetEntityId();
           default:
               throw new IllegalStateException("Relationship Id was not loaded. for relationship " + getDefinition().toString());
        }
    }

    public TARGET_CLASS getTargetEntity() {
        switch (getLoadLevel()){
            case FULL:
                return this.relationshipEntry.getTargetEntity();
            case ID:
                throw new IllegalStateException("Relationship entity was not loaded in Full level (Id only) for relationship " + getDefinition().toString());
            default:
                throw new IllegalStateException("Relationship was not loaded. for relationship " + getDefinition().toString());
        }
    }

    public void setTargetEntityId(Key targetEntityId) {
        if (this.relationshipEntry == null){
            this.relationshipEntry = (RelationshipEntry<TARGET_CLASS>) getDefinition().getRelationshipEntryDescriptor().instance();
            this.relationshipEntry.setSourceEntityId(getOwnerEntity().getId());
            this.relationshipEntry.setTargetEntityId(targetEntityId);
        }else {
            this.relationshipEntry.setTargetEntityId(targetEntityId);
        }
        setLoadLevelForce(RelationshipLoadLevel.ID);
    }

    public void setTargetEntity(TARGET_CLASS targetEntity) {
        if (this.relationshipEntry == null){
            this.relationshipEntry = (RelationshipEntry<TARGET_CLASS>) getDefinition().getRelationshipEntryDescriptor().instance();
            this.relationshipEntry.setSourceEntityId(getOwnerEntity().getId());
            this.relationshipEntry.setTargetEntity(targetEntity);
        }else {
            this.relationshipEntry.setTargetEntity(targetEntity);
        }
        setLoadLevelForce(RelationshipLoadLevel.FULL);
    }
}
