package com.shpandrak.datamodel.field;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/31/13
 * Time: 14:41
 */
public class EntityKeyFieldDescriptor extends KeyFieldDescriptor<EntityKey> {
    public EntityKeyFieldDescriptor(String name, boolean keyField) {
        super(name, keyField);
    }

    public EntityKeyFieldDescriptor(String name) {
        super(name);
    }

    @Override
    public Class<EntityKey> getFieldClassType() {
        return EntityKey.class;
    }

    @Override
    public EntityKey fromString(String value) {
        return new EntityKey(value);
    }
}
