package com.shpandrak.database.converters;

import com.shpandrak.database.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with honest love
 * User: shpandrak
 * Date: 8/30/12
 * Time: 3:33 PM
 */
public class BooleanQueryConverter extends PrimitiveQueryConverter<Boolean> {
    public BooleanQueryConverter(String columnName) {
        super(columnName);
    }

    public BooleanQueryConverter(int pos) {
        super(pos);
    }

    @Override
    public Boolean convert(ResultSet resultSet) throws SQLException {
        if (columnName == null){
            boolean aBoolean = resultSet.getBoolean(pos);
            if (resultSet.wasNull()) return null;
            return aBoolean;
        }else {
            boolean aBoolean = resultSet.getBoolean(columnName);
            if (resultSet.wasNull()) return null;
            return aBoolean;
        }

    }
}
