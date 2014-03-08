package com.shpandrak.database.query.parser;

import com.shpandrak.database.table.DBField;
import com.shpandrak.persistence.query.filter.BasicFieldFilterCondition;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/23/13
 * Time: 09:16
 */
class BasicFieldFilterConditionQueryParser implements IFilterQueryParser<BasicFieldFilterCondition> {

    @Override
    public void parse(BasicFieldFilterCondition condition, QueryParsingContext context) {
        DBField dbField = context.getTable().getFieldsMap().get(condition.getField().getName());
        context.addSimpleQueryCondition(new SimpleQueryCondition(dbField.getPersistentFieldName(), condition.getOperatorType().getSqlRepresentation(), "?", dbField.prepareForPersisting(condition.getValue())));
    }
}
