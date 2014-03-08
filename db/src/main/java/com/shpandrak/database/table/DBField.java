package com.shpandrak.database.table;

import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.persistence.PersistentField;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/20/12
 * Time: 14:14
 */
public abstract class DBField<T> extends PersistentField<T>{
    private DBTable table;

    protected DBField(FieldDescriptor<T> fieldDescriptor, String persistentFieldName) {
        super(fieldDescriptor, persistentFieldName);
    }

    protected DBField(FieldDescriptor<T> fieldDescriptor) {
        super(fieldDescriptor);
    }

    public IQueryConverter<T> getQueryConverter(){
        return getQueryConverter(null);
    }

    public T convert(ResultSet resultSet, String tableAlias) throws SQLException {
        return getQueryConverter(tableAlias).convert(resultSet);
    }

    public abstract IQueryConverter<T> getQueryConverter(String tableAlias);

    public DBTable getTable() {
        return table;
    }

    public void setTable(DBTable table) {
        this.table = table;
    }

    @Override
    public String toString() {
        String fieldName = getPersistentFieldName();
        if (table != null){
            fieldName = table.getName() + "." + fieldName;
        }
        return fieldName;
    }
}
