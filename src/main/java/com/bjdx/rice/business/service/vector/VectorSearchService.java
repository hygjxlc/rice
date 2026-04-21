package com.bjdx.rice.business.service.vector;

import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.Product;

import java.util.List;

/**
 * 向量检索服务接口
 * 提供基于语义相似度的客户和商品匹配能力
 * 
 * @author Rice System
 */
public interface VectorSearchService {

    /**
     * 搜索相似客户
     * @param customerName 客户名称
     * @param topK 返回结果数
     * @param threshold 相似度阈值
     * @return 匹配结果，包含客户ID和相似度分数
     */
    VectorSearchResult searchCustomer(String customerName, int topK, float threshold);

    /**
     * 搜索相似商品
     * @param productName 商品名称
     * @param topK 返回结果数
     * @param threshold 相似度阈值
     * @return 匹配结果，包含商品ID和相似度分数
     */
    VectorSearchResult searchProduct(String productName, int topK, float threshold);

    /**
     * 搜索相似商品（返回多个候选结果）
     * 用于规格二次区分场景，当多个候选LCS分数接近时，通过规格比较选择最佳匹配
     * @param productName 商品名称（含规格，如"海天1.9升鲜味生抽"）
     * @param topK 返回候选结果数
     * @param threshold 相似度阈值
     * @return 候选匹配结果列表，按LCS分数降序排列
     */
    List<VectorSearchResult> searchProductCandidates(String productName, int topK, float threshold);

    /**
     * 检查服务是否可用
     * @return true if available
     */
    boolean isAvailable();

    /**
     * 向量检索结果
     */
    class VectorSearchResult {
        private Long id;
        private String name;
        private float score;
        private Object entity;

        public VectorSearchResult(Long id, String name, float score) {
            this.id = id;
            this.name = name;
            this.score = score;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public Object getEntity() {
            return entity;
        }

        public void setEntity(Object entity) {
            this.entity = entity;
        }

        public boolean isSuccess() {
            return id != null && score > 0;
        }
    }
}
