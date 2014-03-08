package com.shpandrak.database.managers;

import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.table.DBField;
import com.shpandrak.database.table.DBTable;
import com.shpandrak.datamodel.IShpandrakObject;
import com.shpandrak.datamodel.ShpandrakObjectDescriptor;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.datamodel.field.FieldInstance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/22/12
 * Time: 13:29
 */
public class ShpandrakObjectQueryConverter<T extends IShpandrakObject> implements IQueryConverter<T> {

    protected DBTable dbTable;
    protected String tableAlias;
    protected ShpandrakObjectDescriptor objectDescriptor;

    public ShpandrakObjectQueryConverter(DBTable dbTable, String tableAlias, ShpandrakObjectDescriptor objectDescriptor) {
        this.dbTable = dbTable;
        this.tableAlias = tableAlias;
        this.objectDescriptor = objectDescriptor;
    }

    @Override
    public T convert(ResultSet resultSet) throws SQLException {
        List<DBField> dbFields = dbTable.getFields();
        T instance = (T) objectDescriptor.instance();
        Map<String,FieldInstance> fieldsMap = instance.getFieldsMap();
        for (DBField currDBField : dbFields) {
            FieldDescriptor fieldDescriptor = currDBField.getFieldDescriptor();
            ShpandrakObjectDescriptor currObjectDescriptor = fieldDescriptor.getObjectDescriptor();
            if (currObjectDescriptor != null && currObjectDescriptor.equals(objectDescriptor)){
                fieldsMap.get(fieldDescriptor.getName()).setValue(currDBField.convert(resultSet, tableAlias));
            }
        }
        return instance;
    }
}
