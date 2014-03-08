package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.shpandrak.persistence.PersistenceException;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/11/13
 * Time: 10:44
 */
public class DefaultGDSConnectionProvider implements GDSConnectionProvider{

    private DatastoreService datastoreService = null;
    private Transaction transaction = null;

    @Override
    public DatastoreService getDatastoreService() {
        if (datastoreService == null){
            datastoreService = DatastoreServiceFactory.getDatastoreService();
        }
        return datastoreService;
    }

    @Override
    public Transaction getActiveTransaction() {
        return transaction;
    }

    @Override
    public void beginTransaction() throws PersistenceException {
        transaction = getDatastoreService().beginTransaction();
    }

    @Override
    public void commitTransaction() throws PersistenceException {
        if (transaction == null){
            throw new PersistenceException("Transaction cannot be committed because there is no active transaction in context");
        }
        transaction.commit();
        transaction = null;
    }

    @Override
    public void rollbackTransaction() throws PersistenceException {
        if (transaction == null){
            throw new PersistenceException("Transaction cannot be rolled back because there is no active transaction in context");
        }
        if (transaction.isActive()){
            transaction.rollback();
        }
        transaction = null;
    }

    @Override
    public boolean isInTransaction() throws PersistenceException {
        return transaction != null;
    }

    @Override
    public void destroy() throws PersistenceException {
        if (transaction != null){
            transaction.rollback();
            transaction = null;
            throw new PersistenceException("Transaction rolled back while destroying the connection");
        }
        datastoreService = null;
    }
}
