package com.shpandrak.database.table;

import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.converters.StringQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.StringFieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:19
 */
public class DBStringField extends DBField<String> {

    public DBStringField(StringFieldDescriptor fieldDescriptor) {
        super(fieldDescriptor);
    }

    public DBStringField(FieldDescriptor<String> fieldDescriptor, String persistentFieldName) {
        super(fieldDescriptor, persistentFieldName);
    }

    @Override
    public IQueryConverter<String> getQueryConverter(String tableAlias) {
        return new StringQueryConverter(DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public Object prepareForPersisting(String value) {
        return value;
    }
}
