package com.shpandrak.database.connection;

import com.shpandrak.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created with love
 * User: shpandrak
 * Date: 2/11/13
 * Time: 08:40
 */
public class SimpleJDBCThreadLocalConnectionProvider implements IDBConnectionProvider {
    private static final Logger logger = LoggerFactory.getLogger(SimpleJDBCThreadLocalConnectionProvider.class);

    private String jdbcDriver;
    private String connectionString;
    private String dbUser;
    private String dbPassword;
    private Connection connection = null;

    public SimpleJDBCThreadLocalConnectionProvider(String jdbcDriver, String connectionString, String dbUser, String dbPassword) {
        this.jdbcDriver = jdbcDriver;
        this.connectionString = connectionString;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public boolean hasActiveConnection(){
        return connection != null;
    }


    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null){
            logger.debug("Creating a connection for thread {}, dbUser {}", Thread.currentThread().getName(), dbUser);
            connection = createNewConnection();

        }else {
            logger.debug("Found an existing connection on thread {}", Thread.currentThread().getName());
        }

        return connection;

    }

    private Connection createNewConnection() throws SQLException {
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load jdbc driver " + jdbcDriver, e);
        }
        return DriverManager.getConnection(connectionString, dbUser, dbPassword);
    }

    @Override
    public void returnConnection(Connection connection) throws SQLException {
        // Nothing - only relevant when using connection pooling
        logger.debug("Connection returned on thread {}", Thread.currentThread().getName());
    }


    @Override
    public void destroy() throws PersistenceException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new PersistenceException("Failed Closing database connection on thread " + Thread.currentThread(), e);
        }finally {
            connection = null;
        }

    }

    @Override
    public void beginTransaction() throws PersistenceException {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new PersistenceException("Failed starting a database transaction", e);
        }
    }

    @Override
    public void commitTransaction() throws PersistenceException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new PersistenceException("Failed committing database transaction", e);
        }
        setAutoCommitOn();
    }

    @Override
    public void rollbackTransaction() throws PersistenceException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new PersistenceException("Failed rolling back database transaction", e);
        }
        setAutoCommitOn();


    }

    @Override
    public boolean isInTransaction() throws PersistenceException {
        try {
            return !connection.getAutoCommit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setAutoCommitOn() throws PersistenceException {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new PersistenceException("Failed setting database auto commit on", e);
        }
    }
}
