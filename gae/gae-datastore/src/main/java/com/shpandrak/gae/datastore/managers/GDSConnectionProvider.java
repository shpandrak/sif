package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;
import com.shpandrak.persistence.IConnectionProvider;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/11/13
 * Time: 10:44
 */
public interface GDSConnectionProvider extends IConnectionProvider{
    DatastoreService getDatastoreService();
    Transaction getActiveTransaction();
}
