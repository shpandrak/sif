package com.shpandrak.gae.datastore.query;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.shpandrak.datamodel.field.FieldDescriptor;
import com.shpandrak.gae.datastore.managers.GDSPersistableObjectAdapter;
import com.shpandrak.persistence.query.filter.BasicFieldFilterCondition;
import com.shpandrak.persistence.query.filter.FilterConditionOperatorType;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/15/13
 * Time: 15:10
 */
public class GDSBasicFieldFilterConditionQueryParser implements IGDSFilterQueryParser<BasicFieldFilterCondition>{
    @Override
    public Query.Filter parse(BasicFieldFilterCondition condition, GDSQueryParsingContext context) {

        FieldDescriptor field = condition.getField();
        Object value = GDSPersistableObjectAdapter.getFieldForGDS(field.getFieldType(), condition.getValue());
        return new Query.FilterPredicate(field.getName(), getOperator(condition.getOperatorType()), value);
    }

    private Query.FilterOperator getOperator(FilterConditionOperatorType operatorType) {
        switch (operatorType){
            case EQUALS:
                return Query.FilterOperator.EQUAL;
            case NOT_EQUALS:
                return Query.FilterOperator.NOT_EQUAL;
            case GREATER_OR_EQUALS:
                return Query.FilterOperator.GREATER_THAN_OR_EQUAL;
            case GREATER_THEN:
                return Query.FilterOperator.GREATER_THAN;
            case LESS_OR_EQUALS:
                return Query.FilterOperator.LESS_THAN_OR_EQUAL;
            case LESS_THEN:
                return  Query.FilterOperator.LESS_THAN;
            default:
                throw new IllegalArgumentException("Unsupported query operator " + operatorType);
        }
    }
}
