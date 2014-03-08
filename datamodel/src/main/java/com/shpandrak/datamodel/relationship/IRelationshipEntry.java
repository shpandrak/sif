package com.shpandrak.datamodel.relationship;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.datamodel.IShpandrakObject;
import com.shpandrak.datamodel.field.Key;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/28/12
 * Time: 09:48
 */
public interface IRelationshipEntry<TARGET_CLASS extends BaseEntity> extends IShpandrakObject{
    Key getTargetEntityId();
    void setTargetEntityId(Key targetEntityId);
    void setTargetEntity(TARGET_CLASS targetEntity);
    TARGET_CLASS getTargetEntity();
}
