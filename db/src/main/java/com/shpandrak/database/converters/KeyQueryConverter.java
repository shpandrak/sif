package com.shpandrak.database.converters;

import com.shpandrak.datamodel.field.Key;
import com.shpandrak.datamodel.field.KeyFieldDescriptor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 8/30/12
 * Time: 3:33 PM
 */
public class KeyQueryConverter extends PrimitiveQueryConverter<Key> {
    private KeyFieldDescriptor<Key> keyFieldDescriptor;

    public KeyQueryConverter(KeyFieldDescriptor<Key> keyFieldDescriptor, String columnName) {
        super(columnName);
        this.keyFieldDescriptor = keyFieldDescriptor;
    }

    public KeyQueryConverter(KeyFieldDescriptor<Key> keyFieldDescriptor, int pos) {
        super(pos);
        this.keyFieldDescriptor = keyFieldDescriptor;
    }

    @Override
    public Key convert(ResultSet resultSet) throws SQLException {
        String uuidString;
        if (columnName == null){
            uuidString = resultSet.getString(pos);
        }else {
            uuidString = resultSet.getString(columnName);
        }

        if (uuidString == null) return null;
        return keyFieldDescriptor.fromString(uuidString);
    }
}
