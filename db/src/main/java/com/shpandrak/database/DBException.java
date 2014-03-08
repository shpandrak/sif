package com.shpandrak.database;

import com.shpandrak.persistence.PersistenceException;

import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/24/12
 * Time: 09:08
 */
public class DBException extends PersistenceException{
    private String sql;
    private List<Object> params;

    public DBException(String message, String sql, List<Object> params, Throwable cause) {
        super(message + ":" + cause.getMessage() + "; sql=" + sql, cause);
        this.sql = sql;
        this.params = params;
    }

    public DBException(String message, Throwable cause) {
        super(message + ":" + cause.getMessage(), cause);
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParams() {
        return params;
    }

}
