package com.shpandrak.database.converters;

import com.shpandrak.database.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/17/12
 * Time: 08:41
 */
public class DateQueryConverter extends PrimitiveQueryConverter<Date> {
    public DateQueryConverter(int pos) {
        super(pos);
    }

    public DateQueryConverter(String columnName) {
        super(columnName);
    }

    public DateQueryConverter() {
    }

    @Override
    public Date convert(ResultSet resultSet) throws SQLException {
        Long resultSetLong;

        if (usePos()){
            resultSetLong = resultSet.getLong(pos);
        }else {
            resultSetLong = resultSet.getLong(columnName);
        }
        if (resultSetLong == null){
            return null;
        }
        return new Date(resultSetLong);
    }

}
