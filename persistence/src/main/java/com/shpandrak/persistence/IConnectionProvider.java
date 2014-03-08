package com.shpandrak.persistence;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/20/13
 * Time: 09:22
 */
public interface IConnectionProvider {
    void beginTransaction() throws PersistenceException;
    void commitTransaction() throws PersistenceException;
    void rollbackTransaction() throws PersistenceException;
    boolean isInTransaction() throws PersistenceException;
    void destroy() throws PersistenceException;

}
