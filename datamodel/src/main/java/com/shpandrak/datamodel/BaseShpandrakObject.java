package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.FieldInstance;

import java.util.*;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/22/12
 * Time: 12:00
 */
public abstract class BaseShpandrakObject implements IShpandrakObject {

    // Field Instances map by name
    private Map<String, FieldInstance> fieldInstanceMap;

    // Ordered FieldInstance list
    private List<FieldInstance> orderedFields;


    protected void initialize(Collection<FieldInstance> fieldInstances){
        this.orderedFields = new ArrayList<FieldInstance>(fieldInstances);
        this.fieldInstanceMap = new HashMap<String, FieldInstance>(fieldInstances.size());
        for (FieldInstance currFieldInstance : fieldInstances) {
            this.fieldInstanceMap.put(currFieldInstance.getName(), currFieldInstance);
        }
    }

    @Override
    public List<FieldInstance> getFields() {
        return orderedFields;
    }

    @Override
    public Map<String, FieldInstance> getFieldsMap() {
        return fieldInstanceMap;
    }

    @Override
    public <F> FieldInstance<F> getFieldsInstance(FieldDescriptor<F> descriptor) {
        return fieldInstanceMap.get(descriptor.getName());
    }
}
