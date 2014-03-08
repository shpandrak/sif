package com.shpandrak.datamodel;

import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.FieldInstance;

import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/17/12
 * Time: 20:08
 */
public interface IShpandrakObject {
    // Object Descriptor instance
    ShpandrakObjectDescriptor getObjectDescriptor();

    // Ordered field values map
    List<FieldInstance> getFields();

    // Object field values by name
    Map<String, FieldInstance> getFieldsMap();

    // Get specific field instance
    <F> FieldInstance<F> getFieldsInstance(FieldDescriptor<F> descriptor);
}
