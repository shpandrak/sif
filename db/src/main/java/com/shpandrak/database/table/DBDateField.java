package com.shpandrak.database.table;

import com.shpandrak.database.converters.DateQueryConverter;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.DateFieldDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;

import java.util.Date;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/17/12
 * Time: 08:40
 */
public class DBDateField extends DBField<Date> {

    public DBDateField(DateFieldDescriptor fieldDescriptor) {
        super(fieldDescriptor);
    }

    public DBDateField(FieldDescriptor<Date> fieldDescriptor, String persistentFieldName) {
        super(fieldDescriptor, persistentFieldName);
    }

    @Override
    public IQueryConverter<Date> getQueryConverter(String tableAlias) {
        return new DateQueryConverter(DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public Object prepareForPersisting(Date value) {
        if (value == null){
            return null;
        }else {
            return value.getTime();
        }
    }
}
