package com.bjdx.rice.business.service.vector.impl;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.CustomerVector;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.entity.ProductVector;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.bjdx.rice.business.mapper.CustomerVectorMapper;
import com.bjdx.rice.business.mapper.ProductMapper;
import com.bjdx.rice.business.mapper.ProductVectorMapper;
import com.bjdx.rice.business.service.vector.EmbeddingService;
import com.bjdx.rice.business.service.vector.VectorSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量同步服务实现
 * 
 * @author Rice System
 */
@Service
public class VectorSyncServiceImpl implements VectorSyncService {

    private static final Logger logger = LoggerFactory.getLogger(VectorSyncServiceImpl.class);

    @Autowired
    private VectorSearchProperties vectorProperties;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    @Qualifier("pgVectorJdbcTemplate")
    private JdbcTemplate pgVectorJdbcTemplate;

    @Autowired
    private CustomerInfoMapper customerInfoMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CustomerVectorMapper customerVectorMapper;

    @Autowired
    private ProductVectorMapper productVectorMapper;

    @Override
    @Async("vectorSyncExecutor")
    public void syncCustomersAsync(List<CustomerInfo> customers) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return;
        }

        try {
            logger.info("Starting async sync for {} customers", customers.size());
            
            int batchSize = 100;
            for (int i = 0; i < customers.size(); i += batchSize) {
                List<CustomerInfo> batch = customers.subList(i, Math.min(i + batchSize, customers.size()));
                syncCustomerBatch(batch);
            }
            
            logger.info("Completed async sync for {} customers", customers.size());
        } catch (Exception e) {
            logger.error("Failed to sync customers", e);
        }
    }

    @Override
    @Async("vectorSyncExecutor")
    public void syncProductsAsync(List<Product> products) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return;
        }

        try {
            logger.info("Starting async sync for {} products", products.size());
            
            int batchSize = 100;
            for (int i = 0; i < products.size(); i += batchSize) {
                List<Product> batch = products.subList(i, Math.min(i + batchSize, products.size()));
                syncProductBatch(batch);
            }
            
            logger.info("Completed async sync for {} products", products.size());
        } catch (Exception e) {
            logger.error("Failed to sync products", e);
        }
    }

    @Override
    public void syncCustomer(CustomerInfo customer) {
        if (!vectorProperties.getSearch().isEnabled() || customer == null) {
            return;
        }

        try {
            String text = customer.getUnitName();
            if (customer.getUnitAlias() != null && !customer.getUnitAlias().isEmpty()) {
                text += " " + customer.getUnitAlias();
            }

            float[] vector = embeddingService.embed(text);
            
            CustomerVector cv = new CustomerVector();
            cv.setId(customer.getId());
            cv.setUnitName(customer.getUnitName());
            cv.setUnitAlias(customer.getUnitAlias());
            cv.setVector(vector);
            
            customerVectorMapper.upsert(cv);
            
            logger.debug("Synced customer vector: {}", customer.getUnitName());
        } catch (Exception e) {
            logger.error("Failed to sync customer: {}", customer.getUnitName(), e);
        }
    }

    @Override
    public void syncProduct(Product product) {
        if (!vectorProperties.getSearch().isEnabled() || product == null) {
            return;
        }

        try {
            String text = product.getProductName();
            if (product.getProductCode() != null && !product.getProductCode().isEmpty()) {
                text += " " + product.getProductCode();
            }

            float[] vector = embeddingService.embed(text);
            
            ProductVector pv = new ProductVector();
            pv.setId(product.getId());
            pv.setProductName(product.getProductName());
            pv.setProductCode(product.getProductCode());
            pv.setVector(vector);
            
            productVectorMapper.upsert(pv);
            
            logger.debug("Synced product vector: {}", product.getProductName());
        } catch (Exception e) {
            logger.error("Failed to sync product: {}", product.getProductName(), e);
        }
    }

    @Override
    public void rebuildAllCustomers() {
        logger.info("Starting full rebuild of customer vectors to PostgreSQL");
        try {
            // 确保表存在
            createCustomerVectorsTableIfNotExists();
            
            // 清空现有数据
            pgVectorJdbcTemplate.execute("TRUNCATE TABLE customer_vectors");
            logger.info("Truncated customer_vectors table");
            
            // 查询所有客户
            logger.info("Querying all customers...");
            List<CustomerInfo> customers = customerInfoMapper.selectAll();
            logger.info("Found {} customers to sync", customers.size());
            
            if (customers.isEmpty()) {
                logger.warn("No customers found to sync");
                return;
            }
            
            // 批量同步到 PostgreSQL
            int batchSize = 100;
            int successCount = 0;
            for (int i = 0; i < customers.size(); i += batchSize) {
                List<CustomerInfo> batch = customers.subList(i, Math.min(i + batchSize, customers.size()));
                try {
                    syncCustomerBatchToPG(batch);
                    successCount += batch.size();
                    logger.info("Synced {}/{} customers to PostgreSQL", successCount, customers.size());
                } catch (Exception e) {
                    logger.error("Failed to sync batch {}/{}: {}", i / batchSize + 1, (customers.size() + batchSize - 1) / batchSize, e.getMessage());
                }
            }
            
            logger.info("Completed full rebuild of customer vectors to PostgreSQL");
        } catch (Exception e) {
            logger.error("Failed to rebuild all customer vectors", e);
        }
    }

    @Override
    public void rebuildAllProducts() {
        logger.info("Starting full rebuild of product vectors to PostgreSQL");
        try {
            // 确保表存在
            createProductVectorsTableIfNotExists();
            
            // 清空现有数据
            pgVectorJdbcTemplate.execute("TRUNCATE TABLE product_vectors");
            logger.info("Truncated product_vectors table");
            
            // 查询所有商品
            logger.info("Querying all products...");
            List<Product> products = productMapper.selectAll();
            logger.info("Found {} products to sync", products.size());
            
            if (products.isEmpty()) {
                logger.warn("No products found to sync");
                return;
            }
            
            // 批量同步到 PostgreSQL
            int batchSize = 100;
            int successCount = 0;
            for (int i = 0; i < products.size(); i += batchSize) {
                List<Product> batch = products.subList(i, Math.min(i + batchSize, products.size()));
                try {
                    syncProductBatchToPG(batch);
                    successCount += batch.size();
                    logger.info("Synced {}/{} products to PostgreSQL", successCount, products.size());
                } catch (Exception e) {
                    logger.error("Failed to sync batch {}/{}: {}", i / batchSize + 1, (products.size() + batchSize - 1) / batchSize, e.getMessage());
                }
            }
            
            logger.info("Completed full rebuild of product vectors to PostgreSQL");
        } catch (Exception e) {
            logger.error("Failed to rebuild all product vectors", e);
        }
    }

    @Override
    public void deleteCustomerVector(Long customerId) {
        if (customerId != null) {
            customerVectorMapper.deleteById(customerId);
            logger.debug("Deleted customer vector: {}", customerId);
        }
    }

    @Override
    public void deleteProductVector(Long productId) {
        if (productId != null) {
            productVectorMapper.deleteById(productId);
            logger.debug("Deleted product vector: {}", productId);
        }
    }

    private void syncCustomerBatch(List<CustomerInfo> customers) {
        List<String> texts = customers.stream()
                .map(c -> c.getUnitName() + (c.getUnitAlias() != null ? " " + c.getUnitAlias() : ""))
                .collect(Collectors.toList());

        List<float[]> vectors = embeddingService.embedBatch(texts);
        
        List<CustomerVector> customerVectors = new ArrayList<>();
        for (int i = 0; i < customers.size(); i++) {
            CustomerInfo c = customers.get(i);
            CustomerVector cv = new CustomerVector();
            cv.setId(c.getId());
            cv.setUnitName(c.getUnitName());
            cv.setUnitAlias(c.getUnitAlias());
            cv.setVector(vectors.get(i));
            customerVectors.add(cv);
        }

        customerVectorMapper.batchInsert(customerVectors);
    }

    private void syncProductBatch(List<Product> products) {
        List<String> texts = products.stream()
                .map(p -> p.getProductName() + (p.getProductCode() != null ? " " + p.getProductCode() : ""))
                .collect(Collectors.toList());

        List<float[]> vectors = embeddingService.embedBatch(texts);
        
        List<ProductVector> productVectors = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            ProductVector pv = new ProductVector();
            pv.setId(p.getId());
            pv.setProductName(p.getProductName());
            pv.setProductCode(p.getProductCode());
            pv.setSpecification(p.getSpecification());
            pv.setVector(vectors.get(i));
            productVectors.add(pv);
        }

        productVectorMapper.batchInsert(productVectors);
    }

    private void syncCustomerBatchToPG(List<CustomerInfo> customers) {
        List<String> texts = customers.stream()
                .map(c -> c.getUnitName() + (c.getUnitAlias() != null ? " " + c.getUnitAlias() : ""))
                .collect(Collectors.toList());

        List<float[]> vectors = embeddingService.embedBatch(texts);
        
        String sql = "INSERT INTO customer_vectors (id, unit_name, unit_alias, vector) VALUES (?, ?, ?, ?::vector(1024)) " +
                     "ON CONFLICT (id) DO UPDATE SET unit_name = EXCLUDED.unit_name, unit_alias = EXCLUDED.unit_alias, vector = EXCLUDED.vector";
        
        for (int i = 0; i < customers.size(); i++) {
            CustomerInfo c = customers.get(i);
            float[] vector = vectors.get(i);
            String vectorStr = arrayToVectorString(vector);
            pgVectorJdbcTemplate.update(sql, c.getId(), c.getUnitName(), c.getUnitAlias(), vectorStr);
        }
    }

    private void syncProductBatchToPG(List<Product> products) {
        List<String> texts = products.stream()
                .map(p -> p.getProductName() + (p.getProductCode() != null ? " " + p.getProductCode() : ""))
                .collect(Collectors.toList());

        List<float[]> vectors = embeddingService.embedBatch(texts);
        
        String sql = "INSERT INTO product_vectors (id, product_name, product_code, specification, vector) VALUES (?, ?, ?, ?, ?::vector(1024)) " +
                     "ON CONFLICT (id) DO UPDATE SET product_name = EXCLUDED.product_name, product_code = EXCLUDED.product_code, specification = EXCLUDED.specification, vector = EXCLUDED.vector";
        
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            float[] vector = vectors.get(i);
            String vectorStr = arrayToVectorString(vector);
            pgVectorJdbcTemplate.update(sql, p.getId(), p.getProductName(), p.getProductCode(), p.getSpecification(), vectorStr);
        }
    }

    private String arrayToVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", vector[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    private void createCustomerVectorsTableIfNotExists() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS customer_vectors (" +
                    "id BIGINT PRIMARY KEY, " +
                    "unit_name VARCHAR(255), " +
                    "unit_alias VARCHAR(255), " +
                    "vector vector(1024), " +
                    "metadata TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            pgVectorJdbcTemplate.execute(sql);
            logger.info("Customer vectors table created or already exists in PostgreSQL");
        } catch (Exception e) {
            logger.error("Failed to create customer_vectors table in PostgreSQL", e);
        }
    }

    private void createProductVectorsTableIfNotExists() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS product_vectors (" +
                    "id BIGINT PRIMARY KEY, " +
                    "product_name VARCHAR(255), " +
                    "product_code VARCHAR(50), " +
                    "specification VARCHAR(100), " +
                    "vector vector(1024), " +
                    "metadata TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            pgVectorJdbcTemplate.execute(sql);
            logger.info("Product vectors table created or already exists in PostgreSQL");
        } catch (Exception e) {
            logger.error("Failed to create product_vectors table in PostgreSQL", e);
        }
    }
}
