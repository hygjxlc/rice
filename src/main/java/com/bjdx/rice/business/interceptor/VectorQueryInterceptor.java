package com.bjdx.rice.business.interceptor;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.bjdx.rice.business.mapper.ProductMapper;
import com.bjdx.rice.business.service.vector.VectorSearchService;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

/**
 * 向量查询拦截器
 * 拦截 getCustomerByName 和 getProductByName 方法，当字符串匹配失败时启用向量检索
 * 
 * @author Rice System
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class VectorQueryInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(VectorQueryInterceptor.class);

    private VectorSearchProperties vectorProperties;

    private VectorSearchService vectorSearchService;

    private CustomerInfoMapper customerInfoMapper;

    private ProductMapper productMapper;

    // Setter methods for dependency injection
    public void setVectorProperties(VectorSearchProperties vectorProperties) {
        this.vectorProperties = vectorProperties;
    }

    public void setVectorSearchService(VectorSearchService vectorSearchService) {
        this.vectorSearchService = vectorSearchService;
    }

    public void setCustomerInfoMapper(CustomerInfoMapper customerInfoMapper) {
        this.customerInfoMapper = customerInfoMapper;
    }

    public void setProductMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 先执行原查询
        Object result = invocation.proceed();

        // 3. 检查是否需要向量增强
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        String methodId = ms.getId();

        // 2. 如果未启用向量检索，直接返回
        if (!vectorProperties.getSearch().isEnabled()) {
            return result;
        }

        // 客户查询增强
        if (methodId.endsWith("getCustomerByName")) {
            return enhanceCustomerQuery(result, invocation.getArgs()[1]);
        }

        // 商品查询增强
        if (methodId.endsWith("getProductByName")) {
            return enhanceProductQuery(result, invocation.getArgs()[1]);
        }

        return result;
    }

    /**
     * 增强客户查询
     */
    private Object enhanceCustomerQuery(Object result, Object parameter) {
        String customerName = extractParameter(parameter, "customerName");
        
        // 检查 SQL 结果是否有效
        boolean hasValidResult = false;
        if (result instanceof CustomerInfo) {
            hasValidResult = ((CustomerInfo) result).getId() != null;
        } else if (result != null) {
            hasValidResult = true;
        }
        
        // 如果原查询已有有效结果，直接返回
        if (hasValidResult) {
            logger.debug("Vector拦截器: 客户SQL查询已有结果'{}'，跳过向量搜索", customerName);
            return result;
        }
        
        if (customerName == null || customerName.trim().isEmpty()) {
            return result;
        }

        if (vectorSearchService == null || !vectorSearchService.isAvailable()) {
            logger.warn("Vector拦截器: 向量服务不可用，无法增强客户查询");
            return result;
        }

        try {
            logger.info("Vector拦截器: 客户SQL未匹配'{}'，启动向量搜索", customerName);
            
            VectorSearchService.VectorSearchResult vsResult = vectorSearchService.searchCustomer(
                    customerName,
                    vectorProperties.getSearch().getMaxResults(),
                    vectorProperties.getSearch().getSimilarityThreshold()
            );

            if (vsResult != null && vsResult.isSuccess()) {
                logger.info("Vector拦截器: 客户匹配成功 '{}' -> '{}' (相似度: {})",
                        customerName, vsResult.getName(), String.format("%.4f", vsResult.getScore()));
                return vsResult.getEntity();
            } else {
                logger.warn("Vector拦截器: 客户向量搜索未匹配 '{}'", customerName);
            }
        } catch (Exception e) {
            logger.error("Vector拦截器: 客户向量搜索异常 '{}'", customerName, e);
        }

        return result;
    }

    /**
     * 增强商品查询
     */
    private Object enhanceProductQuery(Object result, Object parameter) {
        String productName = extractParameter(parameter, "productName");
        
        // 检查 SQL 结果是否有效
        boolean hasValidResult = false;
        if (result instanceof Product) {
            hasValidResult = ((Product) result).getId() != null;
        } else if (result != null) {
            hasValidResult = true;
        }
        
        // 如果原查询已有有效结果，直接返回
        if (hasValidResult) {
            logger.debug("Vector拦截器: 商品SQL查询已有结果'{}'，跳过向量搜索", productName);
            return result;
        }
        
        if (productName == null || productName.trim().isEmpty()) {
            return result;
        }

        if (vectorSearchService == null || !vectorSearchService.isAvailable()) {
            logger.warn("Vector拦截器: 向量服务不可用，无法增强商品查询");
            return result;
        }

        try {
            logger.info("Vector拦截器: 商品SQL未匹配'{}'，启动向量搜索", productName);
            
            VectorSearchService.VectorSearchResult vsResult = vectorSearchService.searchProduct(
                    productName,
                    vectorProperties.getSearch().getMaxResults(),
                    vectorProperties.getSearch().getSimilarityThreshold()
            );

            if (vsResult != null && vsResult.isSuccess()) {
                logger.info("Vector拦截器: 商品匹配成功 '{}' -> '{}' (相似度: {})",
                        productName, vsResult.getName(), String.format("%.4f", vsResult.getScore()));
                return vsResult.getEntity();
            } else {
                logger.warn("Vector拦截器: 商品向量搜索未匹配 '{}'", productName);
            }
        } catch (Exception e) {
            logger.error("Vector拦截器: 商品向量搜索异常 '{}'", productName, e);
        }

        return result;
    }

    /**
     * 检查结果是否为空
     */
    private boolean isNotEmpty(Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof List) {
            return !((List<?>) result).isEmpty();
        }
        return true;
    }

    /**
     * 提取参数值
     */
    private String extractParameter(Object parameter, String paramName) {
        if (parameter == null) {
            return null;
        }
        
        // 如果是字符串类型直接返回
        if (parameter instanceof String) {
            return (String) parameter;
        }
        
        // 如果是 Map 类型
        if (parameter instanceof java.util.Map) {
            Object value = ((java.util.Map<?, ?>) parameter).get(paramName);
            return value != null ? value.toString() : null;
        }
        
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 无需额外配置
    }
}
