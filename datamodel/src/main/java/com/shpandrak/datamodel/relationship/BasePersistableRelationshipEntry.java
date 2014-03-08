package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.BasePersistableShpandrakObject;
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
public abstract class BasePersistableRelationshipEntry<TARGET_CLASS extends BaseEntity> extends BasePersistableShpandrakObject implements IRelationshipEntry<TARGET_CLASS>{
    private TARGET_CLASS targetEntity;
    private KeyField sourceEntityId;
    private KeyField targetEntityId;

    protected void initialize(KeyField idField, KeyField sourceEntityIdField, KeyField targetEntityIdField, Collection<FieldInstance> fields){
        initialize(idField, fields);
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
