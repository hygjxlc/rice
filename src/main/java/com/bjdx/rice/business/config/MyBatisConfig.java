package com.bjdx.rice.business.config;

import com.bjdx.rice.business.interceptor.VectorQueryInterceptor;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.bjdx.rice.business.mapper.ProductMapper;
import com.bjdx.rice.business.service.vector.VectorSearchService;
import com.github.pagehelper.PageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Properties;

@Configuration
public class MyBatisConfig {

    @Autowired
    private VectorSearchProperties vectorProperties;

    @Autowired
    @Lazy
    private VectorSearchService vectorSearchService;

    @Autowired
    @Lazy
    private CustomerInfoMapper customerInfoMapper;

    @Autowired
    @Lazy
    private ProductMapper productMapper;

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("params", "count=countSql");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    @Bean
    public VectorQueryInterceptor vectorQueryInterceptor() {
        VectorQueryInterceptor interceptor = new VectorQueryInterceptor();
        interceptor.setVectorProperties(vectorProperties);
        interceptor.setVectorSearchService(vectorSearchService);
        interceptor.setCustomerInfoMapper(customerInfoMapper);
        interceptor.setProductMapper(productMapper);
        return interceptor;
    }
}
