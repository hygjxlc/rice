package com.bjdx.rice.business.entity;

import java.time.LocalDateTime;

/**
 * 商品向量实体
 * 对应 product_vectors 表
 * 
 * @author Rice System
 */
public class ProductVector {
    
    private Long id;
    private String productName;
    private String productCode;
    private String specification;
    private float[] vector;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductVector() {
    }

    public ProductVector(Long id, String productName, String productCode, float[] vector) {
        this.id = id;
        this.productName = productName;
        this.productCode = productCode;
        this.vector = vector;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public float[] getVector() {
        return vector;
    }

    public void setVector(float[] vector) {
        this.vector = vector;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 构建用于生成向量的文本
     */
    public String toEmbeddingText() {
        StringBuilder sb = new StringBuilder();
        sb.append(productName);
        if (productCode != null && !productCode.isEmpty()) {
            sb.append(" ").append(productCode);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ProductVector{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", productCode='" + productCode + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
