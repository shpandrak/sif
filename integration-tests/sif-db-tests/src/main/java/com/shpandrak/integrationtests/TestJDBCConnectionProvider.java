package com.shpandrak.integrationtests;

import com.shpandrak.database.connection.SimpleJDBCThreadLocalConnectionProvider;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/1/13
 * Time: 12:24
 */
public class TestJDBCConnectionProvider extends SimpleJDBCThreadLocalConnectionProvider {
    public TestJDBCConnectionProvider() {
        super("org.h2.Driver", "jdbc:h2:tcp://localhost/~/test", "SA", "");
    }
}
