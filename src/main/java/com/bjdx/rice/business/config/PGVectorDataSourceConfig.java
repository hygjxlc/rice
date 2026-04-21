package com.bjdx.rice.business.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * PGVector 数据源配置
 * 用于向量数据的存储和检索
 */
@Configuration
public class PGVectorDataSourceConfig {

    @Autowired
    private VectorSearchProperties vectorProperties;

    /**
     * PGVector JdbcTemplate - 使用独立的内部数据源，不作为 Spring 主数据源
     */
    @Bean(name = "pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate() {
        VectorSearchProperties.PgVectorProperties pgConfig = vectorProperties.getDatabase().getPgvector();
        
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.postgresql.Driver.class);
        dataSource.setUrl(String.format("jdbc:postgresql://%s:%d/%s",
                pgConfig.getHost(), pgConfig.getPort(), pgConfig.getDatabase()));
        dataSource.setUsername(pgConfig.getUsername());
        dataSource.setPassword(pgConfig.getPassword());
        
        return new JdbcTemplate(dataSource);
    }
}
