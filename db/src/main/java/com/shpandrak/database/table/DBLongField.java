package com.shpandrak.database.table;

import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.converters.LongQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.LongFieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:19
 */
public class DBLongField extends DBField<Long> {
    public DBLongField(LongFieldDescriptor fieldDescriptor) {
        super(fieldDescriptor);
    }


    @Override
    public IQueryConverter<Long> getQueryConverter(String tableAlias) {
        return new LongQueryConverter(DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public Object prepareForPersisting(Long value) {
        return value;
    }
}
