package com.shpandrak.database.connection;

import com.shpandrak.persistence.IConnectionProvider;

import java.sql.Connection;
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
 * Date: 10/20/12
 * Time: 12:26
 */
public interface IDBConnectionProvider extends IConnectionProvider{
    Connection getConnection() throws SQLException;
    void returnConnection(Connection connection) throws SQLException;
}
