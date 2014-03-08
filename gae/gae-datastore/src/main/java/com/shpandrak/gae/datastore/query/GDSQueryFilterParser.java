package com.shpandrak.gae.datastore.query;

import com.google.appengine.api.datastore.Query;
import com.shpandrak.datamodel.BasePersistentObjectDescriptor;
import com.shpandrak.datamodel.OrderByClauseEntry;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.query.filter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with love
 * User: shpandrak
 * Date: 10/25/12
 * Time: 18:34
 */
public class GDSQueryFilterParser {
    private static final Logger logger = LoggerFactory.getLogger(GDSQueryFilterParser.class);
    QueryFilter queryFilter;
    private Query query = null;
    //todo: table shouldn't be here...
    BasePersistentObjectDescriptor descriptor;

    public Query getQuery() {
        return query;
    }
    public GDSQueryFilterParser(QueryFilter queryFilter, BasePersistentObjectDescriptor descriptor) {
        this.queryFilter = queryFilter;
        this.descriptor = descriptor;
    }

    public void parse() throws PersistenceException {

        this.query = new Query(descriptor.getEntityClass().getSimpleName());

        // No filter - return all rows
        if (this.queryFilter == null){
            return;
        }

        // Parse the condition
        FieldFilterCondition condition = queryFilter.getCondition();
        GDSQueryParsingContext context = new GDSQueryParsingContext(query, descriptor);
        Query.Filter filter = null;
        if (condition != null){
            filter = getQueryFilterParser(condition).parse(condition, context);
        }
        if (filter != null){
            query.setFilter(filter);
        }
        if (context.getQueryAncestor() != null){
            query.setAncestor(context.getQueryAncestor());
        }

        if (queryFilter.getOrderByClause() != null){
            for (OrderByClauseEntry currOrderByEntry : queryFilter.getOrderByClause()){
                query.addSort(currOrderByEntry.getFieldName(), currOrderByEntry.isAscending()? Query.SortDirection.ASCENDING :  Query.SortDirection.DESCENDING);
            }
        }

    }


    public static <T extends FieldFilterCondition> IGDSFilterQueryParser<T> getQueryFilterParser(T condition){
        if (condition instanceof BasicFieldFilterCondition) {
            return (IGDSFilterQueryParser<T>) new GDSBasicFieldFilterConditionQueryParser();
        } else if (condition instanceof CompoundFieldFilterCondition){
            return (IGDSFilterQueryParser<T>) new GDSCompoundFieldFilterConditionQueryParser();
        } else if (condition instanceof RelationshipFilterCondition) {
            return (IGDSFilterQueryParser<T>) new GDSRelationshipFilterConditionQueryParser();
        } else{
            throw new UnsupportedOperationException(condition.getClass().getCanonicalName() + " Query condition is currently unsupported");
        }


    }


}
