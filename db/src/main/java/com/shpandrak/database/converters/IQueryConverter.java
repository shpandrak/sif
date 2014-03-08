package com.shpandrak.database.converters;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with love
 * User: shpandrak
 * Date: 8/30/12
 * Time: 3:12 PM
 */
public interface IQueryConverter<T> {
    T convert(ResultSet resultSet) throws SQLException;
}
