package com.shpandrak.database.handlers;

import com.shpandrak.database.IQueryHandler;
import com.shpandrak.database.converters.IQueryConverter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 Copyright (c) 2013, Amit Lieberman
All rights reserved.

                   GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License

 * Created with love.
 * User: shpandrak
 * Date: 8/30/12
 * Time: 2:32 PM
 */
public class MapQueryHandler<TKey, TValue> implements IQueryHandler<Map<TKey, TValue>> {
    private IQueryConverter<TKey> keyConverter;
    private IQueryConverter<TValue> valueConverter;

    public MapQueryHandler(IQueryConverter<TKey> keyConverter, IQueryConverter<TValue> valueConverter) {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    @Override
    public Map<TKey, TValue> handleNativeQueryResult(ResultSet rset) throws SQLException {
        Map<TKey, TValue> map = new HashMap<TKey, TValue>();

        while (rset.next()) {
            map.put(keyConverter.convert(rset), valueConverter.convert(rset));
        }
        return map;
    }

}
