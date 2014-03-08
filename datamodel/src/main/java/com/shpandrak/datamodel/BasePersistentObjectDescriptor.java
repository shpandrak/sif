package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;

import java.util.Collection;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/27/12
 * Time: 20:58
 */
public abstract class BasePersistentObjectDescriptor<T extends IPersistableShpandrakObject> extends ShpandrakObjectDescriptor {
    protected FieldDescriptor<Key> keyFieldDescriptor;
    protected FieldDescriptor appKeyFieldDescriptor;

    protected void initialize(Collection<FieldDescriptor> fieldDescriptors, FieldDescriptor<? extends Key> keyFieldDescriptor) {
        super.initialize(fieldDescriptors);
        this.keyFieldDescriptor = (FieldDescriptor<Key>) keyFieldDescriptor;
        this.appKeyFieldDescriptor = null;
        for (FieldDescriptor currFieldDescriptor : fieldDescriptors){
            if (currFieldDescriptor.isKeyField()){
                this.appKeyFieldDescriptor = currFieldDescriptor;
                break;
            }
        }
    }

    protected BasePersistentObjectDescriptor() {
    }

    protected BasePersistentObjectDescriptor(Collection<FieldDescriptor> fieldDescriptors, FieldDescriptor<Key> keyFieldDescriptor) {
        initialize(fieldDescriptors, keyFieldDescriptor);
    }

    public FieldDescriptor<Key> getKeyFieldDescriptor(){
        return keyFieldDescriptor;
    }

    public FieldDescriptor getAppKeyFieldDescriptor() {
        return appKeyFieldDescriptor;
    }

    @Override
    public IPersistableShpandrakObject instance() {
        return (IPersistableShpandrakObject) super.instance();
    }

    @Override
    public IPersistableShpandrakObject instance(ShpandrakObjectRawData rawData) {
        return (IPersistableShpandrakObject) super.instance(rawData);
    }

    @Override
    public abstract Class<? extends IPersistableShpandrakObject> getEntityClass();
}
