package com.shpandrak.database.table;

import com.shpandrak.database.converters.BooleanQueryConverter;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.BooleanFieldDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:19
 */
public class DBBooleanField extends DBField<Boolean> {

    public DBBooleanField(BooleanFieldDescriptor fieldDescriptor) {
        super(fieldDescriptor);
    }

    public DBBooleanField(FieldDescriptor<Boolean> fieldDescriptor, String persistentFieldName) {
        super(fieldDescriptor, persistentFieldName);
    }

    @Override
    public IQueryConverter<Boolean> getQueryConverter(String tableAlias) {
        return new BooleanQueryConverter(DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public Object prepareForPersisting(Boolean value) {
        return value;
    }
}
