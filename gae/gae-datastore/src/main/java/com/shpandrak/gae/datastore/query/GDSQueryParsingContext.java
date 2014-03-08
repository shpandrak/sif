package com.shpandrak.gae.datastore.query;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;

import java.util.List;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/15/13
 * Time: 15:09
 */
public class GDSQueryParsingContext {
    private Query query;
    private BasePersistentObjectDescriptor descriptor;
    private Key queryAncestor;

    public GDSQueryParsingContext(Query query, BasePersistentObjectDescriptor descriptor) {
        this.query = query;
        this.descriptor = descriptor;
        this.queryAncestor = null;
    }

    public Query getQuery() {
        return query;
    }

    public BasePersistentObjectDescriptor getDescriptor() {
        return descriptor;
    }

    public void setQueryAncestor(Key queryAncestor) {
        this.queryAncestor = queryAncestor;
    }

    public Key getQueryAncestor() {
        return queryAncestor;
    }
}
