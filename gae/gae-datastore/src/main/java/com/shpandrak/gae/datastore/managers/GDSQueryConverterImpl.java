package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.IPersistableShpandrakObject;
import com.shpandrak.datamodel.field.EntityKey;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.FieldInstance;

import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/10/13
 * Time: 21:35
 */
public class GDSQueryConverterImpl<T extends IPersistableShpandrakObject> implements GDSQueryConverter<T> {
    protected BasePersistentObjectDescriptor<T> descriptor;

    public GDSQueryConverterImpl(BasePersistentObjectDescriptor<T> descriptor) {
        this.descriptor = descriptor;
    }

    protected BasePersistentObjectDescriptor<T> getDescriptor() {
        return descriptor;
    }

    @Override
    public T convert(Entity entity) {
        T instance = (T) descriptor.instance();
        // Todo:key
        List<FieldInstance> fields = instance.getFields();
        instance.getFields().get(0).setValue (new EntityKey(KeyFactory.keyToString(entity.getKey())));

        for (int i = 1; i < fields.size(); i++) {
            FieldInstance currFieldEntry = fields.get(i);
            Object property = entity.getProperty(currFieldEntry.getName());
            switch (currFieldEntry.getFieldType()) {
                case ENUM:
                    currFieldEntry.fromString((String) property);
                    break;
                case KEY:
                    Key keyProperty = (Key) property;
                    currFieldEntry.setValue(new EntityKey(KeyFactory.keyToString(keyProperty)));
                    break;
                case INTEGER:
                    if (property == null){
                        currFieldEntry.setValue(null);
                    }else {
                        currFieldEntry.setValue(Integer.valueOf(property.toString()));
                    }
                    break;
                default:
                    currFieldEntry.setValue(property);
                    break;
            }
        }
        return instance;
    }
}
