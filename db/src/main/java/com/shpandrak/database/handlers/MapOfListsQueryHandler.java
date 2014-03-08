package com.shpandrak.database.handlers;

import com.shpandrak.database.IQueryHandler;
import com.shpandrak.database.converters.IQueryConverter;
import com.shpandrak.database.query.DBQueryException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with love
 * User: shpandrak
 * Date: 12/7/12
 * Time: 08:00
 */
public class MapOfListsQueryHandler<TKey, TValue> implements IQueryHandler<Map<TKey, List<TValue>>> {
    private IQueryConverter<TKey> keyConverter;
    private IQueryConverter<TValue> valueConverter;

    public MapOfListsQueryHandler(IQueryConverter<TKey> keyConverter, IQueryConverter<TValue> valueConverter) {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    @Override
    public Map<TKey, List<TValue>> handleNativeQueryResult(ResultSet rset) throws SQLException, DBQueryException {
        Map<TKey, List<TValue>> map = new HashMap<TKey, List<TValue>>();

        while (rset.next()) {
            TKey keyValue = keyConverter.convert(rset);
            List<TValue> list = map.get(keyValue);
            if (list == null){
                list = new ArrayList<TValue>();
                map.put(keyValue, list);
            }
            list.add(valueConverter.convert(rset));
        }
        return map;
    }
}
