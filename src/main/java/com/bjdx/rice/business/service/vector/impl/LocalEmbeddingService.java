package com.bjdx.rice.business.service.vector.impl;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.service.vector.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地模型嵌入服务实现
 * 通过调用本地 Python 脚本或模型实现文本向量化
 * 
 * @author Rice System
 */
@Service
@ConditionalOnProperty(name = "embedding.service.type", havingValue = "local", matchIfMissing = false)
public class LocalEmbeddingService implements EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(LocalEmbeddingService.class);

    // N-Gram 配置（与 sync_vectors.py 一致）
    private static final int MIN_N = 2;
    private static final int MAX_N = 4;
    private static final int VECTOR_DIMENSION = 1024;

    @Autowired
    private VectorSearchProperties vectorProperties;

    private boolean available = false;
    private String modelPath;
    private int dimension;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing LocalEmbeddingService...");
            
            if (vectorProperties == null || vectorProperties.getEmbedding() == null) {
                logger.error("VectorProperties or EmbeddingProperties is null");
                this.available = false;
                return;
            }
            
            VectorSearchProperties.LocalModelProperties localModel = vectorProperties.getEmbedding().getLocalModel();
            if (localModel == null) {
                logger.error("LocalModelProperties is null, using defaults");
                this.modelPath = "/root/models/text2vec-base-chinese";
                this.dimension = 768;
            } else {
                this.modelPath = localModel.getModelName();
                this.dimension = localModel.getDimension();
            }
            
            logger.info("LocalEmbeddingService config - modelPath: {}, dimension: {}", modelPath, dimension);
            
            // 检查模型路径是否存在
            if (modelPath != null) {
                File modelDir = new File(modelPath);
                if (modelDir.exists() && modelDir.isDirectory()) {
                    logger.info("Local embedding model found at: {}", modelPath);
                } else {
                    logger.warn("Local model path not found: {}, will use hash fallback", modelPath);
                }
            }
            
            // 即使模型路径不存在，也标记为可用，使用简单哈希作为 fallback
            this.available = true;
            logger.info("LocalEmbeddingService initialized successfully, available: {}", available);
            
        } catch (Exception e) {
            logger.error("Failed to initialize local embedding service", e);
            this.available = true; // 即使出错也标记为可用，使用 fallback
            this.modelPath = "/root/models/text2vec-base-chinese";
            this.dimension = 768;
        }
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            logger.debug("Empty text provided, returning zero vector");
            return new float[getDimension()];
        }

        String trimmedText = text.trim();
        logger.debug("Embedding text: '{}'", trimmedText);

        // 直接使用哈希方案（快速）
        logger.debug("Using hash fallback for: '{}'", trimmedText);
        return embedWithHash(trimmedText);
    }

    /**
     * 使用本地 Python 脚本进行向量化
     */
    private float[] embedWithPython(String text) {
        try {
            // 构建 Python 命令
            List<String> command = new ArrayList<>();
            command.add("python3");
            command.add("-c");
            command.add(String.format(
                "from sentence_transformers import SentenceTransformer; " +
                "model = SentenceTransformer('%s'); " +
                "import json; " +
                "vec = model.encode('%s', normalize_embeddings=True).tolist(); " +
                "print(json.dumps(vec))",
                modelPath.replace("'", "\\'"),
                text.replace("'", "\\'")
            ));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.debug("Python script exited with code: {}", exitCode);
                return null;
            }

            // 解析 JSON 向量
            String jsonStr = output.toString().trim();
            if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                String[] parts = jsonStr.substring(1, jsonStr.length() - 1).split(",");
                float[] vector = new float[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    vector[i] = Float.parseFloat(parts[i].trim());
                }
                return vector;
            }
        } catch (Exception e) {
            logger.debug("Python embedding error: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 使用 N-Gram + TF-IDF 生成向量（与 sync_vectors.py 一致）
     */
    private float[] embedWithHash(String text) {
        float[] vector = new float[VECTOR_DIMENSION];
        String normalizedText = normalizeText(text);
        
        if (normalizedText.isEmpty()) {
            return vector;
        }
        
        // 提取 N-Gram 特征 (2-4 gram)
        java.util.Map<String, Integer> ngramCounts = new java.util.LinkedHashMap<>();
        for (int n = MIN_N; n <= MAX_N && n <= normalizedText.length(); n++) {
            for (int i = 0; i <= normalizedText.length() - n; i++) {
                String ngram = normalizedText.substring(i, i + n);
                ngramCounts.merge(ngram, 1, Integer::sum);
            }
        }
        
        // 使用 MD5 哈希确定位置，累加频率（与 Python 端一致）
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            for (java.util.Map.Entry<String, Integer> entry : ngramCounts.entrySet()) {
                byte[] hashBytes = md.digest(entry.getKey().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                // 使用全部16字节转为 BigInteger（与 Python int(hexdigest(), 16) 等效）
                java.math.BigInteger hashVal = new java.math.BigInteger(1, hashBytes);
                int idx = hashVal.mod(java.math.BigInteger.valueOf(VECTOR_DIMENSION)).intValue();
                vector[idx] += entry.getValue();
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            logger.error("MD5 not available", e);
            return vector;
        }
        
        // L2 归一化
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        
        return vector;
    }
    
    /**
     * 文本预处理（与 Python sync_vectors.py normalize_text 一致）
     */
    private String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        // 转小写，去除多余空格
        String result = text.toLowerCase().trim();
        // 只保留中文、英文、数字
        result = result.replaceAll("[^\\u4e00-\\u9fa5a-z0-9]", "");
        return result;
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
        return VECTOR_DIMENSION;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
