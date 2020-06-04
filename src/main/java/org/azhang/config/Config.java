package org.azhang.config;

import org.apache.commons.dbcp2.BasicDataSource;
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

@Configuration
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(basePackages = {"org.azhang"})
public class Config {
    @Bean
    @Profile("DataSourceWithoutConnectionPool")
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(DBConfig.getDriverClassName());
        ds.setUrl(DBConfig.getUrl());
        ds.setUsername(DBConfig.getUsername());
        ds.setPassword(DBConfig.getPassword());
        return ds;
    }

    @Bean
    @Profile("DataSourceWithApacheConnectionPool")
    public DataSource dataSource1() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(DBConfig.getDriverClassName());
        ds.setUrl(DBConfig.getUrl());
        ds.setUsername(DBConfig.getUsername());
        ds.setPassword(DBConfig.getPassword());
        ds.setMinEvictableIdleTimeMillis(DBConfig.getMinEvictableIdleTimeMillis());
        ds.setDefaultQueryTimeout(DBConfig.getDefaultQueryTimeout());
        ds.setInitialSize(DBConfig.getInitialSize());
        ds.setMaxTotal(DBConfig.getMaxTotal());
        ds.setMaxIdle(DBConfig.getMaxIdle());
        ds.setMinIdle(DBConfig.getMinIdle());
        ds.setMaxWaitMillis(DBConfig.getMaxWaitMillis());
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
