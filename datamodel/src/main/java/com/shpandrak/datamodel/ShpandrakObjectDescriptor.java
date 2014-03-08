package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.FieldInstance;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 20:36
 */
public abstract class ShpandrakObjectDescriptor {

    public abstract String getEntityName();

    public abstract String getEntityPluralName();

    public abstract Class<? extends IShpandrakObject> getEntityClass();

    private Map<String, FieldDescriptor> fieldDescriptorsMap;

    private List<FieldDescriptor> orderedFieldDescriptors;

    protected void initialize(Collection<FieldDescriptor> fieldDescriptors){
        this.orderedFieldDescriptors = new ArrayList<FieldDescriptor>(fieldDescriptors);
        this.fieldDescriptorsMap = new HashMap<String, FieldDescriptor>(fieldDescriptors.size());
        for (FieldDescriptor currFieldDescriptor : fieldDescriptors) {
            currFieldDescriptor.setObjectDescriptor(this);
            this.fieldDescriptorsMap.put(currFieldDescriptor.getName(), currFieldDescriptor);
        }

    }

    protected ShpandrakObjectDescriptor(Collection<FieldDescriptor> fieldDescriptors) {
        initialize(fieldDescriptors);
    }

    protected ShpandrakObjectDescriptor() {
    }

    public List<FieldDescriptor> getOrderedFieldDescriptors() {
        return orderedFieldDescriptors;
    }

    public IShpandrakObject instance(){
        try {
            return getEntityClass().newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Failed instantiating instance for entity " + getEntityClass().getCanonicalName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed instantiating instance for entity " + getEntityClass().getCanonicalName(), e);
        }
    }

    public IShpandrakObject instance(ShpandrakObjectRawData rawData) {

        IShpandrakObject instance = instance();
        Map<String,FieldInstance> fieldsMap = instance.getFieldsMap();
        if (rawData.getFieldsData() != null){
            for (Map.Entry<String, Object> currRawDataEntry : rawData.getFieldsData().entrySet()){
                FieldInstance fieldInstance = fieldsMap.get(currRawDataEntry.getKey());
                if (fieldInstance == null){
                    throw new IllegalArgumentException("Invalid field name " + currRawDataEntry.getKey() + " for initializing entity " + getEntityName());
                }
                fieldInstance.fromString(currRawDataEntry.getValue().toString());
            }
        }
        return instance;
    }


    public Map<String, FieldDescriptor> getFieldDescriptorsMap() {
        return fieldDescriptorsMap;
    }


}
