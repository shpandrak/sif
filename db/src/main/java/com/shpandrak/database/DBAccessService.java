package com.shpandrak.database;

import com.shpandrak.database.query.QueryService;

import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 21:43
 */
public interface DBAccessService extends QueryService {

    int executeUpdate(String sql) throws DBException;

    List<Integer> executeUpdates(String sql, List<List<Object>> params) throws DBException;

    int executeUpdate(String sql, List<Object> params) throws DBException;

}
