package com.shpandrak.database;

import com.shpandrak.database.query.DBQueryException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 8/30/12
 * Time: 2:43 PM
 */
public interface IQueryHandler<T> {
    T handleNativeQueryResult(ResultSet rset) throws SQLException, DBQueryException;
}
