package com.bjdx.rice.business.service.vector.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.service.vector.EmbeddingService;
import com.bjdx.rice.business.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 嵌入模型服务实现
 * 通过调用外部 Embedding API 实现文本向量化
 * 
 * @author Rice System
 */
@Service
@ConditionalOnProperty(name = "embedding.service.type", havingValue = "api", matchIfMissing = false)
public class ApiEmbeddingService implements EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(ApiEmbeddingService.class);

    @Autowired
    private VectorSearchProperties vectorProperties;

    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[getDimension()];
        }

        try {
            VectorSearchProperties.ApiConfigProperties apiConfig = vectorProperties.getEmbedding().getApiConfig();
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + apiConfig.getApiKey());

            Map<String, Object> body = new HashMap<>();
            body.put("model", apiConfig.getModel());
            body.put("input", text);

            String response = HttpUtil.doPost(apiConfig.getUrl(), "POST", body, headers, null).getResponseData();
            
            return parseEmbeddingResponse(response);
        } catch (Exception e) {
            logger.error("API embedding failed for text: {}", text, e);
            return new float[getDimension()];
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
    public int getDimension() {
        return vectorProperties.getEmbedding().getLocalModel().getDimension();
    }

    @Override
    public boolean isAvailable() {
        try {
            VectorSearchProperties.ApiConfigProperties apiConfig = vectorProperties.getEmbedding().getApiConfig();
            return apiConfig.getUrl() != null && !apiConfig.getUrl().isEmpty()
                    && apiConfig.getApiKey() != null && !apiConfig.getApiKey().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 Embedding API 响应
     * 适配常见的 OpenAI 格式响应
     */
    private float[] parseEmbeddingResponse(String response) {
        JSONObject json = JSON.parseObject(response);
        
        // OpenAI 格式: data[0].embedding
        if (json.containsKey("data")) {
            JSONArray data = json.getJSONArray("data");
            if (data.size() > 0) {
                JSONObject first = data.getJSONObject(0);
                JSONArray embedding = first.getJSONArray("embedding");
                return jsonArrayToFloatArray(embedding);
            }
        }
        
        // 其他格式: embedding
        if (json.containsKey("embedding")) {
            JSONArray embedding = json.getJSONArray("embedding");
            return jsonArrayToFloatArray(embedding);
        }
        
        throw new RuntimeException("Unknown embedding response format");
    }

    private float[] jsonArrayToFloatArray(JSONArray jsonArray) {
        float[] result = new float[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.getFloatValue(i);
        }
        return result;
    }
}
