package com.bjdx.rice.business.service.vector.impl;

import com.bjdx.rice.business.service.vector.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * N-Gram 特征向量生成服务
 * 使用字符级 N-Gram 特征 + TF-IDF 权重生成向量
 * 适合短文本匹配，结果更接近 SQL 字符串匹配
 * 
 * 注意：此服务通过 EmbeddingConfig 配置为 Primary Bean
 */
public class NGramEmbeddingService implements EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(NGramEmbeddingService.class);

    // N-Gram 配置
    private static final int MIN_N = 2;  // 最小 N
    private static final int MAX_N = 4;  // 最大 N
    private static final int VECTOR_DIMENSION = 1024;  // 向量维度（与数据库表一致）

    // 全局词频统计（用于 TF-IDF）
    private Map<String, Integer> globalTermFrequency = new HashMap<>();
    private int totalDocuments = 0;

    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[VECTOR_DIMENSION];
        }

        try {
            // 1. 文本预处理
            String normalizedText = normalizeText(text);

            // 2. 提取 N-Gram 特征
            Map<String, Integer> ngramFreq = extractNGramFeatures(normalizedText);

            // 3. 构建向量
            float[] vector = buildVector(ngramFreq);

            // 4. L2 归一化
            normalizeVector(vector);

            return vector;
        } catch (Exception e) {
            logger.error("NGram embedding failed for text: {}", text, e);
            return new float[VECTOR_DIMENSION];
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> results = new ArrayList<>();
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }

    @Override
    public boolean isAvailable() {
        return true;  // N-Gram 服务始终可用
    }

    @Override
    public int getDimension() {
        return VECTOR_DIMENSION;
    }

    /**
     * 文本预处理
     */
    private String normalizeText(String text) {
        // 转换为小写，去除空格和特殊字符
        return text.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^\\u4e00-\\u9fa5a-z0-9]", "");
    }

    /**
     * 提取 N-Gram 特征
     */
    private Map<String, Integer> extractNGramFeatures(String text) {
        Map<String, Integer> ngramFreq = new HashMap<>();

        for (int n = MIN_N; n <= MAX_N; n++) {
            for (int i = 0; i <= text.length() - n; i++) {
                String ngram = text.substring(i, i + n);
                ngramFreq.merge(ngram, 1, Integer::sum);
            }
        }

        return ngramFreq;
    }

    /**
     * 构建向量（使用哈希技巧将 N-Gram 映射到固定维度）
     */
    private float[] buildVector(Map<String, Integer> ngramFreq) {
        float[] vector = new float[VECTOR_DIMENSION];

        for (Map.Entry<String, Integer> entry : ngramFreq.entrySet()) {
            String ngram = entry.getKey();
            int frequency = entry.getValue();

            // 使用哈希将 N-Gram 映射到向量位置
            int hash = Math.abs(ngram.hashCode()) % VECTOR_DIMENSION;

            // TF-IDF 权重
            float tf = (float) Math.log(1 + frequency);  // 对数 TF
            float idf = calculateIDF(ngram);

            vector[hash] += tf * idf;
        }

        return vector;
    }

    /**
     * 计算 IDF（简化版）
     */
    private float calculateIDF(String term) {
        int docFreq = globalTermFrequency.getOrDefault(term, 1);
        return (float) Math.log((double) (totalDocuments + 1) / (docFreq + 1) + 1);
    }

    /**
     * L2 归一化
     */
    private void normalizeVector(float[] vector) {
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
    }

    /**
     * 计算两个文本的相似度（用于调试）
     */
    public float calculateSimilarity(String text1, String text2) {
        float[] vec1 = embed(text1);
        float[] vec2 = embed(text2);
        return cosineSimilarity(vec1, vec2);
    }

    /**
     * 余弦相似度计算
     */
    private float cosineSimilarity(float[] vec1, float[] vec2) {
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0.0f || norm2 == 0.0f) {
            return 0.0f;
        }

        return dotProduct / ((float) Math.sqrt(norm1) * (float) Math.sqrt(norm2));
    }

    /**
     * 更新全局词频（用于增量训练 IDF）
     */
    public void updateGlobalFrequency(String text) {
        String normalized = normalizeText(text);
        Map<String, Integer> ngrams = extractNGramFeatures(normalized);

        for (String ngram : ngrams.keySet()) {
            globalTermFrequency.merge(ngram, 1, Integer::sum);
        }
        totalDocuments++;
    }
}
