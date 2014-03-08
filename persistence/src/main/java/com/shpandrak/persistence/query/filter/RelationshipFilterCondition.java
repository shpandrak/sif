package com.shpandrak.persistence.query.filter;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.relationship.EntityRelationshipDefinition;
import com.shpandrak.datamodel.relationship.IRelationshipEntry;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/8/12
 * Time: 10:28
 */
public class RelationshipFilterCondition<SOURCE_CLASS extends BaseEntity, TARGET_CLASS extends BaseEntity, REL_ENTRY_CLASS extends IRelationshipEntry<TARGET_CLASS>> extends FieldFilterCondition {
    private EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition;
    private Key relatedEntityId;

    public RelationshipFilterCondition(EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> relationshipDefinition, Key relatedEntityId) {
        this.relationshipDefinition = relationshipDefinition;
        this.relatedEntityId = relatedEntityId;
    }

    public EntityRelationshipDefinition<SOURCE_CLASS, TARGET_CLASS, REL_ENTRY_CLASS> getRelationshipDefinition() {
        return relationshipDefinition;
    }

    public Key getRelatedEntityId() {
        return relatedEntityId;
    }
}
