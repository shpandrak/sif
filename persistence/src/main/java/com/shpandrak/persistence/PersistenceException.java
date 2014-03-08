package com.shpandrak.persistence;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/24/12
 * Time: 09:47
 */
public class PersistenceException extends RuntimeException {
    public PersistenceException(String s) {
        super(s);
    }

    public PersistenceException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
