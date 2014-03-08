package com.shpandrak.gae.datastore.managers;

import com.google.appengine.api.datastore.Entity;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/10/13
 * Time: 21:30
 */
public interface GDSQueryConverter<T> {
    T convert(Entity entity);
}
