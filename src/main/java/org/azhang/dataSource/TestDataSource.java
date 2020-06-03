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
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Named
@Profile("DataSourceWithImplementedConnectionPool")
public class TestDataSource implements DataSource {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestDataSource.class);
    private final Vector<Connection> connections = new Vector<Connection>();
    private final AtomicInteger numOfConnections;

    @Inject
    public TestDataSource() {
        int initialSize = DBConfig.getInitialSize();
        numOfConnections = new AtomicInteger(initialSize);
        for (int i = 0; i < initialSize; i++) {
            try {
                createNewConnection();
            } catch (SQLException e) {
                logger.error("Fail to connect Mysql.", e);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (connections.size() > 0) {
            final Connection connection = connections.firstElement();
            connections.remove(0);
            logger.info("Get connection, connection pool size is : {}", connections.size());

            return (Connection) Proxy.newProxyInstance(TestDataSource.class.getClassLoader(),
                    connection.getClass().getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equalsIgnoreCase("close")) {
                                connections.add(connection);
                                logger.info("Connection returns to connection pool, pool size is : {}", connections.size());
                                return null;
                            } else {
                                return method.invoke(connection, args);
                            }
                        }
                    });
        } else if (numOfConnections.get() < DBConfig.getMaxTotal()) {
            Connection connection = createNewConnection();
            numOfConnections.getAndIncrement();
            return connection;
        } else {
            try {
                wait(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getConnection();
        }
    }

    private Connection createNewConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DBConfig.getUrl(), DBConfig.getUsername(), DBConfig.getPassword());
        logger.info("Get connection: {}.", connection);
        connections.add(connection);
        return connection;
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
