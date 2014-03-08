package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.FieldInstance;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyField;

import java.util.Collection;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/22/12
 * Time: 12:00
 */
public abstract class BasePersistableShpandrakObject extends BaseShpandrakObject implements IPersistableShpandrakObject {
    private KeyField id;

    protected void initialize(KeyField idField, Collection<FieldInstance> fieldInstances) {
        super.initialize(fieldInstances);
        this.id = idField;
    }

    @Override
    public Key getId() {
        return id.getValue();
    }

    @Override
    public void setId(Key id) {
        this.id.setValue(id);
    }
}
