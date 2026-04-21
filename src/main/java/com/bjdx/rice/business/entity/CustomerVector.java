package com.bjdx.rice.business.entity;

import java.time.LocalDateTime;

/**
 * 客户向量实体
 * 对应 customer_vectors 表
 * 
 * @author Rice System
 */
public class CustomerVector {
    
    private Long id;
    private String unitName;
    private String unitAlias;
    private float[] vector;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerVector() {
    }

    public CustomerVector(Long id, String unitName, String unitAlias, float[] vector) {
        this.id = id;
        this.unitName = unitName;
        this.unitAlias = unitAlias;
        this.vector = vector;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitAlias() {
        return unitAlias;
    }

    public void setUnitAlias(String unitAlias) {
        this.unitAlias = unitAlias;
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
        sb.append(unitName);
        if (unitAlias != null && !unitAlias.isEmpty()) {
            sb.append(" ").append(unitAlias);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "CustomerVector{" +
                "id=" + id +
                ", unitName='" + unitName + '\'' +
                ", unitAlias='" + unitAlias + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
