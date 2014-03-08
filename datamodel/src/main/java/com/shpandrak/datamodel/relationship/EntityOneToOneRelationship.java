package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.field.Key;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/23/12
 * Time: 15:57
 */
public class EntityOneToOneRelationship<SOURCE_CLASS extends BaseEntity, TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends RelationshipEntry<TARGET_CLASS>> extends EntityRelationship<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> {
    @Override
    public boolean isSatisfied() {
        //todo:do
        return false;
    }

    @Override
    public void prepareForPersisting() {
        //todo:do or remove this patch
    }

    public EntityOneToOneRelationship(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> definition, SOURCE_CLASS ownerEntity) {
        super(definition, ownerEntity);
    }

    @Override
    public void setOwnerEntityId(Key ownerEntityId) {
        //todo:do
    }
}
