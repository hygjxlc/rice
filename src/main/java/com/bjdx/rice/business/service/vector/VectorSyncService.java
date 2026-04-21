package com.bjdx.rice.business.service.vector;

import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.Product;

import java.util.List;

/**
 * 向量同步服务接口
 * 负责将业务数据同步到向量数据库
 * 
 * @author Rice System
 */
public interface VectorSyncService {

    /**
     * 异步同步客户数据
     * @param customers 客户列表
     */
    void syncCustomersAsync(List<CustomerInfo> customers);

    /**
     * 异步同步商品数据
     * @param products 商品列表
     */
    void syncProductsAsync(List<Product> products);

    /**
     * 同步单个客户
     * @param customer 客户信息
     */
    void syncCustomer(CustomerInfo customer);

    /**
     * 同步单个商品
     * @param product 商品信息
     */
    void syncProduct(Product product);

    /**
     * 全量重建客户向量
     */
    void rebuildAllCustomers();

    /**
     * 全量重建商品向量
     */
    void rebuildAllProducts();

    /**
     * 删除客户向量
     * @param customerId 客户ID
     */
    void deleteCustomerVector(Long customerId);

    /**
     * 删除商品向量
     * @param productId 商品ID
     */
    void deleteProductVector(Long productId);
}
