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
public class DoubleQueryConverter extends PrimitiveQueryConverter<Double> {

    public DoubleQueryConverter(int pos) {
        super(pos);
    }

    public DoubleQueryConverter(String columnName) {
        super(columnName);
    }

    @Override
    public Double convert(ResultSet resultSet) throws SQLException {
        double aDouble;
        if (columnName == null){
            aDouble = resultSet.getDouble(pos);
        }else {
            aDouble = resultSet.getDouble(columnName);
        }
        if (resultSet.wasNull()){
            return null;
        }
        return aDouble;
    }
}
