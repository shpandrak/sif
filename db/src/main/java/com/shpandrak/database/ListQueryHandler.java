package com.shpandrak.database;

import com.shpandrak.database.converters.IQueryConverter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with love.
 * User: shpandrak
 * Date: 8/30/12
 * Time: 2:45 PM
 */
public class ListQueryHandler<T> implements IQueryHandler<List<T>> {

    private IQueryConverter<T> converter;

    public ListQueryHandler(IQueryConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public List<T> handleNativeQueryResult(ResultSet rset) throws SQLException {
        List<T> resultList = new ArrayList<T>();
        while (rset.next()) {
            resultList.add(converter.convert(rset));
        }
        return resultList;
    }
}
