package com.bjdx.rice.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 向量检索配置属性
 * 配置前缀: vector
 * 
 * @author Rice System
 */
@Component
@ConfigurationProperties(prefix = "vector")
public class VectorSearchProperties {

    /**
     * 向量检索搜索配置
     */
    private SearchProperties search = new SearchProperties();

    /**
     * 文本嵌入模型配置
     */
    private EmbeddingProperties embedding = new EmbeddingProperties();

    /**
     * 向量数据库配置
     */
    private DatabaseProperties database = new DatabaseProperties();

    /**
     * 向量同步配置
     */
    private SyncProperties sync = new SyncProperties();

    public SearchProperties getSearch() {
        return search;
    }

    public void setSearch(SearchProperties search) {
        this.search = search;
    }

    public EmbeddingProperties getEmbedding() {
        return embedding;
    }

    public void setEmbedding(EmbeddingProperties embedding) {
        this.embedding = embedding;
    }

    public DatabaseProperties getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseProperties database) {
        this.database = database;
    }

    public SyncProperties getSync() {
        return sync;
    }

    public void setSync(SyncProperties sync) {
        this.sync = sync;
    }

    /**
     * 搜索相关配置
     */
    public static class SearchProperties {
        /**
         * 是否启用向量匹配功能（默认关闭）
         */
        private boolean enabled = false;

        /**
         * 相似度阈值（0.0-1.0），低于此值认为不匹配
         */
        private float similarityThreshold = 0.75f;

        /**
         * 最大返回结果数
         */
        private int maxResults = 3;

        /**
         * 是否记录匹配日志
         */
        private boolean logMatches = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public float getSimilarityThreshold() {
            return similarityThreshold;
        }

        public void setSimilarityThreshold(float similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public boolean isLogMatches() {
            return logMatches;
        }

        public void setLogMatches(boolean logMatches) {
            this.logMatches = logMatches;
        }
    }

    /**
     * 嵌入模型配置
     */
    public static class EmbeddingProperties {
        /**
         * 向量化服务类型：local(本地模型) / api(外部API)
         */
        private String modelType = "local";

        /**
         * 本地模型配置
         */
        private LocalModelProperties localModel = new LocalModelProperties();

        /**
         * API配置
         */
        private ApiConfigProperties apiConfig = new ApiConfigProperties();

        public String getModelType() {
            return modelType;
        }

        public void setModelType(String modelType) {
            this.modelType = modelType;
        }

        public LocalModelProperties getLocalModel() {
            return localModel;
        }

        public void setLocalModel(LocalModelProperties localModel) {
            this.localModel = localModel;
        }

        public ApiConfigProperties getApiConfig() {
            return apiConfig;
        }

        public void setApiConfig(ApiConfigProperties apiConfig) {
            this.apiConfig = apiConfig;
        }
    }

    /**
     * 本地模型配置
     */
    public static class LocalModelProperties {
        /**
         * 模型名称或路径
         */
        private String modelName = "shibing624/text2vec-base-chinese";

        /**
         * 向量维度
         */
        private int dimension = 768;

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }
    }

    /**
     * API配置
     */
    public static class ApiConfigProperties {
        /**
         * API地址
         */
        private String url = "";

        /**
         * API密钥
         */
        private String apiKey = "";

        /**
         * 模型名称
         */
        private String model = "embedding-v1";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    /**
     * 向量数据库配置
     */
    public static class DatabaseProperties {
        /**
         * 数据库类型：pgvector / milvus
         */
        private String type = "pgvector";

        /**
         * PGVector配置
         */
        private PgVectorProperties pgvector = new PgVectorProperties();

        /**
         * Milvus配置
         */
        private MilvusProperties milvus = new MilvusProperties();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public PgVectorProperties getPgvector() {
            return pgvector;
        }

        public void setPgvector(PgVectorProperties pgvector) {
            this.pgvector = pgvector;
        }

        public MilvusProperties getMilvus() {
            return milvus;
        }

        public void setMilvus(MilvusProperties milvus) {
            this.milvus = milvus;
        }
    }

    /**
     * PGVector配置
     */
    public static class PgVectorProperties {
        private String host = "localhost";
        private int port = 5432;
        private String database = "rice_vector";
        private String username = "postgres";
        private String password = "";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Milvus配置
     */
    public static class MilvusProperties {
        private String host = "localhost";
        private int port = 19530;
        private String collectionPrefix = "rice";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getCollectionPrefix() {
            return collectionPrefix;
        }

        public void setCollectionPrefix(String collectionPrefix) {
            this.collectionPrefix = collectionPrefix;
        }
    }

    /**
     * 向量同步配置
     */
    public static class SyncProperties {
        /**
         * 是否启用启动时自动同步（默认关闭）
         */
        private boolean autoSyncOnStartup = false;

        public boolean isAutoSyncOnStartup() {
            return autoSyncOnStartup;
        }

        public void setAutoSyncOnStartup(boolean autoSyncOnStartup) {
            this.autoSyncOnStartup = autoSyncOnStartup;
        }
    }
}
