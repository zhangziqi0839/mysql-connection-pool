package org.azhang;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(basePackages = {"org.azhang"})
public class Config {
    @Bean
    @Profile("DataSourceWithoutConnectionPool")
    public DataSource dataSource(@Value("${spring.datasource.url}") String url,
                                 @Value("${spring.datasource.username}") String username,
                                 @Value("${spring.datasource.password}") String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        System.out.println(com.mysql.cj.jdbc.Driver.class);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean
    @Profile("DataSourceWithApacheConnectionPool")
    public DataSource dataSource1(@Value("${spring.datasource.url}") String url,
                                 @Value("${spring.datasource.username}") String username,
                                 @Value("${spring.datasource.password}") String password) {
        BasicDataSource ds = new BasicDataSource();
        Properties prop = new Properties();

        try {
            prop.load(Config.class.getClassLoader().getResourceAsStream("application.properties"));
            ds.setDriverClassName(prop.getProperty("spring.datasource.driver"));
            ds.setUrl(prop.getProperty("spring.datasource.url"));
            ds.setUsername(prop.getProperty("spring.datasource.username"));
            ds.setPassword(prop.getProperty("spring.datasource.password"));
            ds.setMinEvictableIdleTimeMillis(Long.parseLong(prop.getProperty("minEvictableIdleTimeMillis")));
            ds.setDefaultQueryTimeout(Integer.parseInt(prop.getProperty("defaultQueryTimeoutSeconds")));
            ds.setInitialSize(Integer.parseInt(prop.getProperty("initialSize")));
            ds.setMaxTotal(Integer.parseInt(prop.getProperty("maxActive")));
            ds.setMaxIdle(Integer.parseInt(prop.getProperty("maxIdle")));
            ds.setMinIdle(Integer.parseInt(prop.getProperty("minIdle")));
            ds.setMaxWaitMillis(Integer.parseInt(prop.getProperty("maxWait")));
        } catch(IOException e){
            System.out.println("Fail to read db configuration: " + e.getMessage());
        }

        return ds;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setGenerateDdl(true);
        return jpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
        emfb.setDataSource(dataSource);
        emfb.setJpaVendorAdapter(jpaVendorAdapter);
        emfb.setPackagesToScan("org.azhang");
        return emfb;
    }
}
