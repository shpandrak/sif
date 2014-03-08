package com.shpandrak.database;

import com.shpandrak.database.converters.IQueryConverter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with love.
 * User: shpandrak
 * Date: 8/30/12
 * Time: 2:45 PM
 */
public class SetQueryHandler<T> implements IQueryHandler<Set<T>> {

    private IQueryConverter<T> converter;

    public SetQueryHandler(IQueryConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public Set<T> handleNativeQueryResult(ResultSet rset) throws SQLException {
        Set<T> resultList = new HashSet<T>();
        while (rset.next()) {
            resultList.add(converter.convert(rset));
        }
        return resultList;
    }
}
