package com.shpandrak.database.query;

import com.shpandrak.database.DBException;
import com.shpandrak.database.IQueryHandler;
import com.shpandrak.database.ListQueryHandler;
import com.shpandrak.database.SetQueryHandler;
import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.database.handlers.MapOfListsQueryHandler;
import com.shpandrak.database.handlers.MapQueryHandler;
import com.shpandrak.database.converters.IQueryConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 8/30/12
 * Time: 11:10 AM
 */
public class QueryServiceBean implements QueryService {
    private static final Logger logger = LoggerFactory.getLogger(QueryServiceBean.class);

    private IDBConnectionProvider connectionProvider;

    public QueryServiceBean(IDBConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public <T> List<T> getList(String sql, IQueryConverter<T> converter, List<Object> params) throws DBException{
        return executeQuery(sql, new ListQueryHandler<T>(converter), params);
    }

    @Override
    public <T> List<T> getList(String sql, IQueryConverter<T> converter) throws DBException {
        return getList(sql, converter, null);
    }

    @Override
    public <T> Set<T> getSet(String sql, IQueryConverter<T> converter, List<Object> params) throws DBException {
        return executeQuery(sql, new SetQueryHandler<T>(converter), params);
    }

    @Override
    public <T> Set<T> getSet(String sql, IQueryConverter<T> converter) throws DBException {
        return getSet(sql, converter, null);
    }

    protected <T> T executeQuery(String sql, IQueryHandler<T> queryHandler, List<Object> params) throws DBException {
        logger.debug("executing query: {}", sql);
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        long tickBefore = -1, tickAfter = -1;

        try {
            statement = setPreparedStatement(sql, params);
            tickBefore = System.currentTimeMillis();
            resultSet = statement.executeQuery();
            tickAfter = System.currentTimeMillis();
            return queryHandler.handleNativeQueryResult(resultSet);
        } catch (Exception e) {
            if (e instanceof DBException){
                throw (DBException)e;
            }else{
                throw new DBQueryException("Failed executing query", sql, params, e);
            }
        } finally {
            closeRsetStmt(statement, resultSet);
            if (statement != null && logger.isDebugEnabled()) {
                logger.debug((statement + (tickBefore != -1 && tickAfter != -1 ? " (Total time=" + (tickAfter - tickBefore) + ")" : "")));
            }
        }
    }


    @Override
    public <TKey, TValue> Map<TKey, TValue> getMap(String sql, IQueryConverter<TKey> keyConverter, IQueryConverter<TValue> valueConverter, List<Object> params) throws DBException {
        return executeQuery(sql, new MapQueryHandler<TKey, TValue>(keyConverter, valueConverter), params);
    }

    @Override
    public <TKey, TValue> Map<TKey, List<TValue>> getMapOfLists(String sql, IQueryConverter<TKey> keyConverter, IQueryConverter<TValue> valueConverter, List<Object> params) throws DBException {
        return executeQuery(sql, new MapOfListsQueryHandler<TKey, TValue>(keyConverter, valueConverter), params);
    }

    protected void setStatement(PreparedStatement stmt, String sql, List<Object> params) throws DBException {
        try {
            stmt.clearParameters();

            if (params != null) {
                int paramOrdinal = 1;
                for (Object param : params) {
                    //Convert parameter of type util Date to TimeStamp.
                    if (param instanceof Date) {
                        param = ((Date) param).getTime();
                    }

                    if (param == null) {
                        stmt.setObject(paramOrdinal, param);
                        paramOrdinal++;
                    } else {
                        stmt.setObject(paramOrdinal, param);
                        paramOrdinal++;
                    }
                }
            }
        } catch (SQLException e) {
            closeRsetStmt(stmt);

            throw new DBException("Failed to set params to preparedStatement", sql, params, e);
        }
    }

    protected PreparedStatement getPreparedStatement(String sql) throws DBException {
        Connection conn = null;
        PreparedStatement stmt;
        try {
            conn = connectionProvider.getConnection();
            stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    connectionProvider.returnConnection(conn);
                }
            } catch (SQLException e1) {
                throw new DBException("Failed getting prepared statement", sql, null, e1);
            }

            throw new DBException(sql, e);
        }

        return stmt;
    }

    protected PreparedStatement setPreparedStatement(String sql, List<Object> params) throws DBException {
        PreparedStatement stmt = getPreparedStatement(sql);
        setStatement(stmt, sql, params);
        return stmt;
    }

    protected void closeRsetStmt(Statement pstmt, ResultSet rs) throws DBException {
        Connection conn = null;

        try {
            if (pstmt != null) {
                conn = pstmt.getConnection();
                pstmt.close();
            } else if (rs != null && rs.getStatement() != null) {
                conn = rs.getStatement().getConnection();
                rs.getStatement().close();
            }
            if (rs != null) rs.close();
            if (conn != null) connectionProvider.returnConnection(conn);
        } catch (SQLException e) {
            throw new DBException("Failed to close result set and statement", e);
        }
    }

    protected void closeRsetStmt(Statement pstmt) throws DBException {
        closeRsetStmt(pstmt, null);
    }

    @Override
    public <T> T getSingleValue(final String sql, final IQueryConverter<T> converter, final List<Object> params){
        return executeQuery(sql, new IQueryHandler<T>() {
            @Override
            public T handleNativeQueryResult(ResultSet rset) throws SQLException, DBQueryException {
                if (!rset.next()) {
                    return null;
                }
                T singleValue = converter.convert(rset);
                if (rset.next()) {
                    throw new DBQueryException("Single row expected but fetched multiple rows", sql, params, null);
                }
                return singleValue;
            }
        }, params);
    }

/*
    protected <TKey, TValue> Map<TKey, List<TValue>> getMapOfListsBySQL(String sql, List<Object> params, IRsetValueConverter<TKey> keyConverter, IRsetValueConverter<TValue> valueConverter) throws DBException
    {
        MapOfListsRsetHandler<TKey, TValue> handler = new MapOfListsRsetHandler<TKey, TValue>(keyConverter, valueConverter);
        getResultSetBySql(sql, params, handler);
        return handler.getMap();
    }

    protected <TKey, TValue> Map<TKey, List<TValue>> getMapOfListsBySQL(String sql, List<Object> params, IRsetValueConverter<TKey> keyConverter, IRsetValueConverter<TValue> valueConverter, String keyColumnName) throws DBException
    {
        MapOfListsRsetHandler<TKey, TValue> handler = new MapOfListsRsetHandler<TKey, TValue>(keyConverter, valueConverter, keyColumnName);
        getResultSetBySql(sql, params, handler);
        return handler.getMap();
    }

    protected <TKey, TValue> Map<TKey, List<TValue>> getMapOfListsBySQL(String sql, List<Object> params, IRsetValueConverter<TKey> keyConverter, IRsetValueConverter<TValue> valueConverter, int keyColumn, int valueColumn) throws DBException
    {
        MapOfListsRsetHandler<TKey, TValue> handler = new MapOfListsRsetHandler<TKey, TValue>(keyConverter, valueConverter, keyColumn, valueColumn);
        getResultSetBySql(sql, params, handler);
        return handler.getMap();
    }

    protected <TKeyOuter, TKeyInner, TValue> Map<TKeyOuter, Map<TKeyInner, TValue>> getMapOfMapsBySQL(String sql, List<Object> params, IRsetValueConverter<TKeyOuter> outerKeyConverter, IRsetValueConverter<TKeyInner> innerKeyConverter, IRsetValueConverter<TValue> valueConverter, String keyInner, String keyOuter, String valueColumn) throws DBException
    {
        MapOfMapsRsetHandler<TKeyOuter, TKeyInner, TValue> handler = new MapOfMapsRsetHandler<TKeyOuter, TKeyInner, TValue>(innerKeyConverter, outerKeyConverter, valueConverter, keyInner, keyOuter, valueColumn);
        getResultSetBySql(sql, params, handler);
        return handler.getMap();
    }

    protected <TKeyOuter, TKeyInner, TValue> Map<TKeyOuter, Map<TKeyInner, TValue>> getMapOfMapsBySQL(String sql, List<Object> params, IRsetValueConverter<TKeyOuter> outerKeyConverter, IRsetValueConverter<TKeyInner> innerKeyConverter, IRsetValueConverter<TValue> valueConverter, int keyInnerPos, int keyOuterPos, int valueColumnPos) throws DBException
    {
        MapOfMapsRsetHandler<TKeyOuter, TKeyInner, TValue> handler = new MapOfMapsRsetHandler<TKeyOuter, TKeyInner, TValue>(innerKeyConverter, outerKeyConverter, valueConverter, keyInnerPos, keyOuterPos, valueColumnPos);
        getResultSetBySql(sql, params, handler);
        return handler.getMap();
    }

    protected <TKey, TValue> Map<TKey, Set<TValue>> getMapOfSetsBySQL(String sql, List<Object> params, IRsetValueConverter<TKey> keyConverter, IRsetValueConverter<TValue> valueConverter) throws DBException
    {
        MapOfSetsRsetHandler<TKey, TValue> handler = new MapOfSetsRsetHandler<TKey, TValue>(keyConverter, valueConverter);
        getResultSetBySql(sql, params, handler);
        return handler.getMap();
    }
*/

}
