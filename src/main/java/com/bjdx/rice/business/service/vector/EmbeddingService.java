package com.bjdx.rice.business.service.vector;

import java.util.List;

/**
 * 文本嵌入服务接口
 * 将文本转换为向量表示
 * 
 * @author Rice System
 */
public interface EmbeddingService {

    /**
     * 单文本向量化
     * @param text 输入文本
     * @return 向量表示（float数组）
     */
    float[] embed(String text);

    /**
     * 批量文本向量化
     * @param texts 输入文本列表
     * @return 向量表示列表
     */
    List<float[]> embedBatch(List<String> texts);

    /**
     * 获取向量维度
     * @return 向量维度（如768）
     */
    int getDimension();

    /**
     * 检查服务是否可用
     * @return true if available
     */
    boolean isAvailable();
}
