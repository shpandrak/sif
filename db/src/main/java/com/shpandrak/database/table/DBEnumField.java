package com.shpandrak.database.table;

import com.shpandrak.database.converters.EnumQueryConverter;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.EnumFieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:19
 */
public class DBEnumField<T extends Enum> extends DBField<T> {

    public DBEnumField(EnumFieldDescriptor<T> fieldDescriptor) {
        super(fieldDescriptor);
    }

    @Override
    public IQueryConverter<T> getQueryConverter(String tableAlias) {
        return new EnumQueryConverter<T>(getFieldDescriptor().getValues(), DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public EnumFieldDescriptor<T> getFieldDescriptor() {
        return (EnumFieldDescriptor<T>) super.getFieldDescriptor();
    }

    @Override
    public Object prepareForPersisting(T value) {
        return value == null? null : value.ordinal();
    }
}
