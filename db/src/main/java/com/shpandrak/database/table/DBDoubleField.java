package com.shpandrak.database.table;

import com.shpandrak.database.converters.DoubleQueryConverter;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.util.DBUtil;
import com.shpandrak.datamodel.field.DoubleFieldDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:19
 */
public class DBDoubleField extends DBField<Double> {

    public DBDoubleField(DoubleFieldDescriptor fieldDescriptor) {
        super(fieldDescriptor);
    }

    public DBDoubleField(FieldDescriptor<Double> fieldDescriptor, String persistentFieldName) {
        super(fieldDescriptor, persistentFieldName);
    }

    @Override
    public IQueryConverter<Double> getQueryConverter(String tableAlias) {
        return new DoubleQueryConverter(DBUtil.nameWithAliasPrefix(getPersistentFieldName(), tableAlias));
    }

    @Override
    public Object prepareForPersisting(Double value) {
        return value;
    }
}
