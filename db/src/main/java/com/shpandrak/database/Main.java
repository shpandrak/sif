package com.shpandrak.database;

import com.shpandrak.database.connection.IDBConnectionProvider;
import com.shpandrak.database.converters.StringQueryConverter;
import com.shpandrak.database.query.QueryServiceBean;
import com.shpandrak.persistence.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 9/18/12
 * Time: 11:08
 */
public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, DBException {
        System.out.println("Hello World");


        Class.forName("org.postgresql.Driver");
        final Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/shpandrak", "shpandrak", "shpandrak");

        List<String> names = new QueryServiceBean(new IDBConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException {
                return conn;
            }

            @Override
            public void returnConnection(Connection connection) throws SQLException {
                //nop
            }

            @Override
            public void beginTransaction() throws PersistenceException {
                //nop
            }

            @Override
            public void commitTransaction() throws PersistenceException {
                //nop
            }

            @Override
            public void rollbackTransaction() throws PersistenceException {
                //nop
            }

            @Override
            public boolean isInTransaction() throws PersistenceException {
                return false;
            }

            @Override
            public void destroy() throws PersistenceException {
                //nop
            }
        }).getList("select * from aaa", new StringQueryConverter("name"));

        conn.close();
        System.out.println(names);
    }
}
