package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.*;
import com.shpandrak.datamodel.field.FieldInstance;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyField;

import java.util.Collection;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/4/12
 * Time: 15:55
 */
public abstract class RelationshipEntry<TARGET_CLASS extends BaseEntity> extends BaseShpandrakObject implements IRelationshipEntry<TARGET_CLASS>{
    protected TARGET_CLASS targetEntity;
    protected KeyField sourceEntityId;
    protected KeyField targetEntityId;

    protected void initialize(KeyField sourceEntityIdField, KeyField targetEntityIdField, Collection<FieldInstance> fields){
        initialize(fields);
        this.sourceEntityId = sourceEntityIdField;
        this.targetEntityId = targetEntityIdField;
    }

    public void setSourceEntityId(Key sourceEntityId) {
        this.sourceEntityId.setValue(sourceEntityId);
    }

    public Key getSourceEntityId() {
        return sourceEntityId.getValue();
    }

    public TARGET_CLASS getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(TARGET_CLASS targetEntity) {
        this.targetEntity = targetEntity;
        if (targetEntity == null){
            this.targetEntityId.setValue(null);
        }else {
            this.targetEntityId.setValue(targetEntity.getId());
        }
    }

    public Key getTargetEntityId() {
        return targetEntityId.getValue();
    }

    public void setTargetEntityId(Key targetEntityId) {
        this.targetEntityId.setValue(targetEntityId);
        if (targetEntityId == null || (this.targetEntity != null && !targetEntityId.equals(this.targetEntity.getId()))){
            this.targetEntity = null;
        }
    }

}
