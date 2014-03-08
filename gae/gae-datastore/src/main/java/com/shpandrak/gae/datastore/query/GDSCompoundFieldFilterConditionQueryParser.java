package com.shpandrak.gae.datastore.query;

import com.google.appengine.api.datastore.Query;
import com.shpandrak.persistence.PersistenceException;
import com.shpandrak.persistence.query.filter.CompoundFieldFilterCondition;
import com.shpandrak.persistence.query.filter.FieldFilterCondition;
import com.shpandrak.persistence.query.filter.FieldFilterLogicalOperatorType;

import java.util.ArrayList;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/15/13
 * Time: 15:25
 */
public class GDSCompoundFieldFilterConditionQueryParser implements IGDSFilterQueryParser<CompoundFieldFilterCondition>{
    @Override
    public Query.Filter parse(CompoundFieldFilterCondition condition, GDSQueryParsingContext context) throws PersistenceException {
        ArrayList<Query.Filter> subFilters = new ArrayList<Query.Filter>(2);
        FieldFilterCondition leftSide = condition.getLeftSide();
        if (leftSide != null){
            Query.Filter filter = GDSQueryFilterParser.getQueryFilterParser(leftSide).parse(leftSide, context);
            if (filter != null){
                subFilters.add(filter);
            }
        }

        FieldFilterCondition rightSide = condition.getRightSide();
        if (rightSide != null){
            Query.Filter filter = GDSQueryFilterParser.getQueryFilterParser(rightSide).parse(rightSide, context);
            if (filter != null){
                subFilters.add(filter);
            }
        }

        if (subFilters.isEmpty()){
            return null;
        }else if (subFilters.size() == 1){
            return subFilters.get(0);
        } else{
            return new Query.CompositeFilter(getLogicalOperator(condition.getLogicalOperatorType()), subFilters);
        }
    }

    private Query.CompositeFilterOperator getLogicalOperator(FieldFilterLogicalOperatorType logicalOperatorType) {
        switch (logicalOperatorType){
            case AND:
                return Query.CompositeFilterOperator.AND;
            case OR:
                return Query.CompositeFilterOperator.OR;
            default:
                throw new IllegalArgumentException("Unsupported logical operator type " + logicalOperatorType);
        }
    }
}
