package com.shpandrak.database;

import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.database.query.QueryServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 21:44
 */
public class DBAccessServiceBean extends QueryServiceBean implements DBAccessService {
    private static final Logger logger = LoggerFactory.getLogger(DBAccessServiceBean.class);

    public DBAccessServiceBean(IDBConnectionProvider connectionProvider) {
        super(connectionProvider);
    }


    public int executeUpdate(String sql) throws DBException {
        return executeUpdate(sql, null);
    }


    public List<Integer> executeUpdates(String sql, List<List<Object>> params) throws DBException {
        if (params.isEmpty())
            return Collections.emptyList();

        List<Integer> retVal = new ArrayList<Integer>(params.size());

        // first prepare the statement
        PreparedStatement stmt = getPreparedStatement(sql);

        List<Object> statementParams = null;
        try {
            for (List<Object> currStatementParams : params) {
                statementParams = currStatementParams;
                setStatement(stmt, sql, currStatementParams);
                retVal.add(stmt.executeUpdate());
            }
        } catch (SQLException e) {
            throw new DBException("Failed executing batch update", sql, statementParams, e);
        } finally {
            if (stmt != null && logger.isDebugEnabled()) {
                logger.debug(stmt.toString());
            }
            closeRsetStmt(stmt, null);
        }
        return retVal;
    }

    public int executeUpdate(String sql, List<Object> params) throws DBException {
        int retVal = 0;
        PreparedStatement stmt = null;

        try {
            stmt = setPreparedStatement(sql, params);
            retVal = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("Failed executing update statement", sql, params, e);
        } finally {
            if (stmt != null && logger.isDebugEnabled()) {
                logger.debug(stmt.toString());
                logger.debug(sql);
            }
            closeRsetStmt(stmt, null);
        }
        return retVal;
    }
}
