package com.shpandrak.database.converters;

import com.shpandrak.database.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with love
 * User: shpandrak
 * Date: 8/30/12
 * Time: 3:33 PM
 */
public class EnumQueryConverter<T extends Enum> extends PrimitiveQueryConverter<T> {
    private T[] values;
    public EnumQueryConverter(T[] values, int pos) {
        super(pos);
        this.values = values;
    }

    public EnumQueryConverter(T[] values, String columnName) {
        super(columnName);
        this.values = values;
    }

    @Override
    public T convert(ResultSet resultSet) throws SQLException {
        if (columnName == null){
            return DBUtil.getEnumFromResultSet(resultSet, pos, values);
        }else {
            return DBUtil.getEnumFromResultSet(resultSet, columnName, values);
        }
    }
}
