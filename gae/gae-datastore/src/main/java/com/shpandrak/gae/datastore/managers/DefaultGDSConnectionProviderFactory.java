package com.shpandrak.gae.datastore.managers;

import com.shpandrak.persistence.IConnectionProvider;
import com.shpandrak.persistence.IConnectionProviderFactory;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/20/13
 * Time: 11:54
 */
public class DefaultGDSConnectionProviderFactory implements IConnectionProviderFactory {
    @Override
    public IConnectionProvider create() {
        return new DefaultGDSConnectionProvider();
    }
}
