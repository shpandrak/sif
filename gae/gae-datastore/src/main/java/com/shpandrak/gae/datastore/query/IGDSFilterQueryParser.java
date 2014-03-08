package com.shpandrak.gae.datastore.query;

import com.google.appengine.api.datastore.Query;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.query.filter.FieldFilterCondition;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/15/13
 * Time: 15:09
 */
public interface IGDSFilterQueryParser<T extends FieldFilterCondition> {
    Query.Filter parse(T condition, GDSQueryParsingContext context) throws PersistenceException;
}
