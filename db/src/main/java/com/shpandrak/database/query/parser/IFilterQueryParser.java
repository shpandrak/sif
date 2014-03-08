package com.shpandrak.database.query.parser;

import com.shpandrak.persistence.query.filter.FieldFilterCondition;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/23/13
 * Time: 09:05
 */
public interface IFilterQueryParser<T extends FieldFilterCondition> {
    void parse(T condition, QueryParsingContext context);
}
