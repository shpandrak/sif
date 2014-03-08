package com.shpandrak.database.table;

import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.converters.KeyQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:18
 */
public class DBKeyField extends DBField<Key>{

    public DBKeyField(KeyFieldDescriptor<? extends Key> fieldDescriptor) {
        super((FieldDescriptor<Key>) fieldDescriptor);
    }

    public DBKeyField(FieldDescriptor<Key> fieldDescriptor, String persistentFieldName) {
        super(fieldDescriptor, persistentFieldName);
    }

    @Override
    public IQueryConverter<Key> getQueryConverter(String tableAlias) {
        return new KeyQueryConverter((KeyFieldDescriptor<Key>) getFieldDescriptor(), DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public Object prepareForPersisting(Key value) {
        return  value == null ? null : value.toPersistentFormat();
    }
}
