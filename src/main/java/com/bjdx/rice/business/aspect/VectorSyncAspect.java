package com.bjdx.rice.business.aspect;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.service.vector.VectorSyncService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 向量同步 AOP 切面
 * 拦截批量插入/更新操作，触发向量同步
 * 
 * @author Rice System
 */
@Aspect
@Component
public class VectorSyncAspect {

    private static final Logger logger = LoggerFactory.getLogger(VectorSyncAspect.class);

    @Autowired
    private VectorSearchProperties vectorProperties;

    @Autowired
    private VectorSyncService vectorSyncService;

    /**
     * 拦截客户批量插入
     */
    @AfterReturning(
        pointcut = "execution(* com.bjdx.rice.business.mapper.CustomerInfoMapper.insertBatch(..))",
        returning = "result"
    )
    public void afterCustomerInsert(JoinPoint joinPoint, Object result) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return;
        }

        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof List) {
                @SuppressWarnings("unchecked")
                List<CustomerInfo> customers = (List<CustomerInfo>) args[0];
                if (!customers.isEmpty()) {
                    vectorSyncService.syncCustomersAsync(customers);
                    logger.debug("Triggered async vector sync for {} customers", customers.size());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to trigger customer vector sync", e);
        }
    }

    /**
     * 拦截客户批量更新
     */
    @AfterReturning(
        pointcut = "execution(* com.bjdx.rice.business.mapper.CustomerInfoMapper.updateBatch(..))",
        returning = "result"
    )
    public void afterCustomerUpdate(JoinPoint joinPoint, Object result) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return;
        }

        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof List) {
                @SuppressWarnings("unchecked")
                List<CustomerInfo> customers = (List<CustomerInfo>) args[0];
                if (!customers.isEmpty()) {
                    vectorSyncService.syncCustomersAsync(customers);
                    logger.debug("Triggered async vector sync for {} customers (update)", customers.size());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to trigger customer vector sync (update)", e);
        }
    }

    /**
     * 拦截商品批量插入
     */
    @AfterReturning(
        pointcut = "execution(* com.bjdx.rice.business.mapper.ProductMapper.insertBatch(..))",
        returning = "result"
    )
    public void afterProductInsert(JoinPoint joinPoint, Object result) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return;
        }

        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof List) {
                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) args[0];
                if (!products.isEmpty()) {
                    vectorSyncService.syncProductsAsync(products);
                    logger.debug("Triggered async vector sync for {} products", products.size());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to trigger product vector sync", e);
        }
    }

    /**
     * 拦截商品批量更新
     */
    @AfterReturning(
        pointcut = "execution(* com.bjdx.rice.business.mapper.ProductMapper.updateBatch(..))",
        returning = "result"
    )
    public void afterProductUpdate(JoinPoint joinPoint, Object result) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return;
        }

        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof List) {
                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) args[0];
                if (!products.isEmpty()) {
                    vectorSyncService.syncProductsAsync(products);
                    logger.debug("Triggered async vector sync for {} products (update)", products.size());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to trigger product vector sync (update)", e);
        }
    }
}
