package com.shpandrak.database.table;

import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.converters.IntegerQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.IntegerFieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:19
 */
public class DBIntegerField extends DBField<Integer> {
    public DBIntegerField(IntegerFieldDescriptor fieldDescriptor) {
        super(fieldDescriptor);
    }


    @Override
    public IQueryConverter<Integer> getQueryConverter(String tableAlias) {
        return new IntegerQueryConverter(DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public Object prepareForPersisting(Integer value) {
        return value;
    }
}
