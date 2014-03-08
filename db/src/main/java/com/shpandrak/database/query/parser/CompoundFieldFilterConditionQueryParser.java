package com.shpandrak.database.query.parser;

import com.shpandrak.persistence.query.filter.CompoundFieldFilterCondition;
import com.shpandrak.persistence.query.filter.FieldFilterCondition;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/23/13
 * Time: 09:49
 */
public class CompoundFieldFilterConditionQueryParser implements IFilterQueryParser<CompoundFieldFilterCondition> {
    @Override
    public void parse(CompoundFieldFilterCondition condition, QueryParsingContext context) {
        switch (condition.getLogicalOperatorType()){
            case AND:
                FieldFilterCondition leftSide = condition.getLeftSide();
                FieldFilterCondition rightSide = condition.getRightSide();
                DBQueryFilterParser.getQueryFilterParser(leftSide).parse(leftSide, context);
                DBQueryFilterParser.getQueryFilterParser(rightSide).parse(rightSide, context);
                break;
            case OR:
                throw new IllegalStateException("OR operator not supported yet :(");
        }

    }
}
