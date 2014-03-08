package com.shpandrak.database.converters;

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
 * Date: 10/8/12
 * Time: 13:32
 */
public abstract class PrimitiveQueryConverter<T> implements IQueryConverter<T> {
    protected int pos = -1;
    protected String columnName = null;

    protected PrimitiveQueryConverter(int pos) {
        this.pos = pos;
    }

    protected PrimitiveQueryConverter(String columnName) {
        this.columnName = columnName;
    }

    protected PrimitiveQueryConverter() {
        this(1);
    }

    protected boolean usePos() {
        return columnName == null;
    }

}
