package com.shpandrak.database.query;

import com.shpandrak.database.DBException;
import com.shpandrak.persistence.PersistenceException;

import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 8/30/12
 * Time: 6:27 PM
 */
public class DBQueryException extends DBException{

    public DBQueryException(String message, String sql, List<Object> params, Throwable cause) {
        super(message, sql, params, cause);
    }

    public DBQueryException(String message, Throwable cause) {
        super(message, cause);
    }

}
