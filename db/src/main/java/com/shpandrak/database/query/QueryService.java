package com.shpandrak.database.query;

import com.shpandrak.database.DBException;
import com.shpandrak.database.converters.IQueryConverter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 8/30/12
 * Time: 11:07 AM
 */
public interface QueryService {

    <T> T getSingleValue(String sql, IQueryConverter<T> converter, List<Object> params);

    <T> List<T> getList(String sql, IQueryConverter<T> converter, List<Object> params);

    <T> List<T> getList(String sql, IQueryConverter<T> converter) throws DBException;

    <T> Set<T> getSet(String sql, IQueryConverter<T> converter, List<Object> params);

    <T> Set<T> getSet(String sql, IQueryConverter<T> converter) throws DBException;

    <TKey, TValue> Map<TKey, TValue> getMap(String sql, IQueryConverter<TKey> keyConverter, IQueryConverter<TValue> valueConverter, List<Object> params);

    <TKey, TValue> Map<TKey, List<TValue>> getMapOfLists(String sql, IQueryConverter<TKey> keyConverter, IQueryConverter<TValue> valueConverter, List<Object> params);
}
