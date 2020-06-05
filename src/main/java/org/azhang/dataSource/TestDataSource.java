package org.azhang.dataSource;

import org.azhang.config.DBConfig;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Named
@Profile("DataSourceWithImplementedConnectionPool")
public class TestDataSource implements DataSource {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestDataSource.class);
    private Vector<Connection> connections = new Vector<Connection>();
    private AtomicInteger numOfConnections = new AtomicInteger();
    private ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<Connection>();
    private final int MAX_RETRY = 3;

    @Inject
    // Initialize connection pool
    public TestDataSource() {
        int initialSize = DBConfig.getInitialSize();
        for (int i = 0; i < initialSize; i++) {
            try {
                connections.add(createNewConnection());
            } catch (SQLException e) {
                logger.error("Fail to connect Mysql.", e);
            }
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        return getConnection(0);
    }

    private synchronized Connection getConnection(int retryTimes) throws SQLException {
        Connection conn = threadLocalConnection.get();
        if (isEnabled(conn)) {
            return conn;
        }

        if (numOfConnections.get() < DBConfig.getMaxTotal()) {
            if (connections.size() > 0) {
                // If there are idle connections in the pool, get connection from pool
                final Connection connection;
                if (isEnabled(connections.firstElement())) {
                    connection = connections.firstElement();
                } else {
                    numOfConnections.getAndDecrement();
                    connection = createNewConnection();
                }
                connections.remove(0);

                logger.info("Get connection, connection pool size is : {}", connections.size());
                // If idle connections are less than minIdle, create a new one
                if (connections.size() < DBConfig.getMinIdle()) {
                    connections.add(createNewConnection());
                }

                // Use proxy instance to operate on connection
                conn = (Connection) Proxy.newProxyInstance(TestDataSource.class.getClassLoader(),
                        connection.getClass().getInterfaces(),
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if (method.getName().equalsIgnoreCase("close")) {
                                    // TODO: is this thread safe?
                                    if (connections.size() >= DBConfig.getMaxIdle()) {
                                        // If idle connections are >= maxIdle, close the returned connection
                                        connection.close();
                                        numOfConnections.getAndDecrement();
                                        logger.info("Close connection: {}", connection);
                                    } else {
                                        // else put it back to pool
                                        connections.add(connection);
                                        logger.info("Connection returns to connection pool, pool size is : {}", connections.size());
                                    }
                                    return null;
                                } else {
                                    return method.invoke(connection, args);
                                }
                            }
                        });
            } else
                // If there are no idle connections and total < maxTotal, create a new connection
                conn = createNewConnection();
        } else {
            // If total connections = maxTotal, wait for maxWaitMillis and retry retrieving connection
            if (retryTimes == MAX_RETRY) {
                throw new RuntimeException("Couldn't get connection since the pool is busy.");
            }
            try {
                wait(DBConfig.getMaxWaitMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            conn = getConnection(++retryTimes);
        }

        threadLocalConnection.set(conn);
        return conn;
    }

    private Connection createNewConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DBConfig.getUrl(), DBConfig.getUsername(), DBConfig.getPassword());
        logger.info("Get connection: {}.", connection);
        numOfConnections.getAndIncrement();
        return connection;
    }

    private boolean isEnabled(Connection connection) throws SQLException {
        if (Objects.isNull(connection)) {
            return false;
        }
        return !connection.isClosed();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    public void setLoginTimeout(int seconds) throws SQLException {

    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
