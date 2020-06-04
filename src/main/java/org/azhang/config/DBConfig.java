package org.azhang.config;

import org.azhang.dataSource.TestDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.Properties;

@Named
public class DBConfig {
    private static final Logger logger = LoggerFactory.getLogger(DBConfig.class);
    private static String driverClassName;
    private static String url;
    private static String username;
    private static String password;
    private static int initialSize;
    private static int maxTotal;
    private static int maxIdle;
    private static int minIdle;
    private static long minEvictableIdleTimeMillis;
    private static int defaultQueryTimeout;
    private static int maxWaitMillis;

    static {
        Properties prop = new Properties();

        try {
            prop.load(TestDataSource.class.getClassLoader().getResourceAsStream("application.properties"));

            driverClassName = prop.getProperty("spring.datasource.driver");
            url = prop.getProperty("spring.datasource.url");
            username = prop.getProperty("spring.datasource.username");
            password = prop.getProperty("spring.datasource.password");
            initialSize = Integer.parseInt(prop.getProperty("initialSize"));
            maxTotal = Integer.parseInt(prop.getProperty("maxActive"));
            maxIdle = Integer.parseInt(prop.getProperty("maxIdle"));
            minIdle = Integer.parseInt(prop.getProperty("minIdle"));
            minEvictableIdleTimeMillis = Long.parseLong(prop.getProperty("minEvictableIdleTimeMillis"));
            defaultQueryTimeout = Integer.parseInt(prop.getProperty("defaultQueryTimeoutSeconds"));
            maxWaitMillis = Integer.parseInt(prop.getProperty("maxWait"));
            //load driver class
            Class.forName(driverClassName);

        } catch(IOException e){
            logger.error("Fail to read db configuration.", e);
        } catch (ClassNotFoundException e) {
            logger.error("Fail to start db driver.", e);
        }
    }

    public static String getDriverClassName() {
        return driverClassName;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static int getInitialSize() {
        return initialSize;
    }

    public static int getMaxTotal() {
        return maxTotal;
    }

    public static int getMaxIdle() {
        return maxIdle;
    }

    public static int getMinIdle() {
        return minIdle;
    }

    public static long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public static int getDefaultQueryTimeout() {
        return defaultQueryTimeout;
    }

    public static int getMaxWaitMillis() {
        return maxWaitMillis;
    }
}
