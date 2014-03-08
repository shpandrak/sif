package com.shpandrak.database.converters;

import java.sql.ResultSet;
import java.sql.SQLException;

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

 * Created with love
 * User: shpandrak
 * Date: 8/30/12
 * Time: 3:33 PM
 */
public class IntegerQueryConverter extends PrimitiveQueryConverter<Integer> {

    public IntegerQueryConverter(int pos) {
        super(pos);
    }

    public IntegerQueryConverter(String columnName) {
        super(columnName);
    }

    @Override
    public Integer convert(ResultSet resultSet) throws SQLException {
        int anInteger;
        if (columnName == null){
            anInteger = resultSet.getInt(pos);
        }else {
            anInteger = resultSet.getInt(columnName);
        }
        if (resultSet.wasNull()){
            return null;
        }
        return anInteger;
    }
}
