package com.bjdx.rice.business.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorSearchProperties 配置类测试
 * 测试向量检索配置属性的默认值和设置
 */
class VectorSearchPropertiesTest {

    private VectorSearchProperties properties;

    @BeforeEach
    void setUp() {
        properties = new VectorSearchProperties();
    }

    // ==================== 搜索配置测试 ====================

    @Nested
    @DisplayName("搜索配置测试")
    class SearchPropertiesTests {

        @Test
        @DisplayName("默认搜索配置值")
        void testDefaultSearchProperties() {
            // Given
            VectorSearchProperties.SearchProperties search = properties.getSearch();

            // Then
            assertNotNull(search);
            assertFalse(search.isEnabled(), "默认应禁用向量检索功能");
            assertEquals(0.75f, search.getSimilarityThreshold(), 0.001f);
            assertEquals(3, search.getMaxResults());
            assertTrue(search.isLogMatches());
        }

        @Test
        @DisplayName("设置搜索属性")
        void testSetSearchProperties() {
            // Given
            VectorSearchProperties.SearchProperties search = new VectorSearchProperties.SearchProperties();
            search.setEnabled(true);
            search.setSimilarityThreshold(0.85f);
            search.setMaxResults(5);
            search.setLogMatches(false);

            // When
            properties.setSearch(search);

            // Then
            assertTrue(properties.getSearch().isEnabled());
            assertEquals(0.85f, properties.getSearch().getSimilarityThreshold(), 0.001f);
            assertEquals(5, properties.getSearch().getMaxResults());
            assertFalse(properties.getSearch().isLogMatches());
        }

        @Test
        @DisplayName("相似度阈值边界值测试")
        void testSimilarityThresholdBoundary() {
            // Given
            VectorSearchProperties.SearchProperties search = properties.getSearch();

            // When & Then - 测试最小值
            search.setSimilarityThreshold(0.0f);
            assertEquals(0.0f, search.getSimilarityThreshold(), 0.001f);

            // When & Then - 测试最大值
            search.setSimilarityThreshold(1.0f);
            assertEquals(1.0f, search.getSimilarityThreshold(), 0.001f);

            // When & Then - 测试中间值
            search.setSimilarityThreshold(0.5f);
            assertEquals(0.5f, search.getSimilarityThreshold(), 0.001f);
        }

        @Test
        @DisplayName("最大结果数边界值测试")
        void testMaxResultsBoundary() {
            // Given
            VectorSearchProperties.SearchProperties search = properties.getSearch();

            // When & Then
            search.setMaxResults(1);
            assertEquals(1, search.getMaxResults());

            search.setMaxResults(10);
            assertEquals(10, search.getMaxResults());

            search.setMaxResults(100);
            assertEquals(100, search.getMaxResults());
        }
    }

    // ==================== 嵌入模型配置测试 ====================

    @Nested
    @DisplayName("嵌入模型配置测试")
    class EmbeddingPropertiesTests {

        @Test
        @DisplayName("默认嵌入配置值")
        void testDefaultEmbeddingProperties() {
            // Given
            VectorSearchProperties.EmbeddingProperties embedding = properties.getEmbedding();

            // Then
            assertNotNull(embedding);
            assertEquals("local", embedding.getModelType());
            assertNotNull(embedding.getLocalModel());
            assertNotNull(embedding.getApiConfig());
        }

        @Test
        @DisplayName("设置嵌入属性")
        void testSetEmbeddingProperties() {
            // Given
            VectorSearchProperties.EmbeddingProperties embedding = new VectorSearchProperties.EmbeddingProperties();
            embedding.setModelType("api");

            // When
            properties.setEmbedding(embedding);

            // Then
            assertEquals("api", properties.getEmbedding().getModelType());
        }

        @Test
        @DisplayName("模型类型测试 - local")
        void testModelTypeLocal() {
            // Given
            VectorSearchProperties.EmbeddingProperties embedding = properties.getEmbedding();
            embedding.setModelType("local");

            // Then
            assertEquals("local", embedding.getModelType());
        }

        @Test
        @DisplayName("模型类型测试 - api")
        void testModelTypeApi() {
            // Given
            VectorSearchProperties.EmbeddingProperties embedding = properties.getEmbedding();
            embedding.setModelType("api");

            // Then
            assertEquals("api", embedding.getModelType());
        }
    }

    // ==================== 本地模型配置测试 ====================

    @Nested
    @DisplayName("本地模型配置测试")
    class LocalModelPropertiesTests {

        @Test
        @DisplayName("默认本地模型配置值")
        void testDefaultLocalModelProperties() {
            // Given
            VectorSearchProperties.LocalModelProperties localModel = properties.getEmbedding().getLocalModel();

            // Then
            assertNotNull(localModel);
            assertEquals("shibing624/text2vec-base-chinese", localModel.getModelName());
            assertEquals(768, localModel.getDimension());
        }

        @Test
        @DisplayName("设置本地模型属性")
        void testSetLocalModelProperties() {
            // Given
            VectorSearchProperties.LocalModelProperties localModel = new VectorSearchProperties.LocalModelProperties();
            localModel.setModelName("custom-model");
            localModel.setDimension(512);

            // When
            properties.getEmbedding().setLocalModel(localModel);

            // Then
            assertEquals("custom-model", properties.getEmbedding().getLocalModel().getModelName());
            assertEquals(512, properties.getEmbedding().getLocalModel().getDimension());
        }

        @Test
        @DisplayName("向量维度常见值测试")
        void testCommonDimensions() {
            // Given
            VectorSearchProperties.LocalModelProperties localModel = properties.getEmbedding().getLocalModel();

            // When & Then - 常见的向量维度
            localModel.setDimension(256);
            assertEquals(256, localModel.getDimension());

            localModel.setDimension(512);
            assertEquals(512, localModel.getDimension());

            localModel.setDimension(768);
            assertEquals(768, localModel.getDimension());

            localModel.setDimension(1024);
            assertEquals(1024, localModel.getDimension());

            localModel.setDimension(1536);
            assertEquals(1536, localModel.getDimension());
        }
    }

    // ==================== API配置测试 ====================

    @Nested
    @DisplayName("API配置测试")
    class ApiConfigPropertiesTests {

        @Test
        @DisplayName("默认API配置值")
        void testDefaultApiConfigProperties() {
            // Given
            VectorSearchProperties.ApiConfigProperties apiConfig = properties.getEmbedding().getApiConfig();

            // Then
            assertNotNull(apiConfig);
            assertEquals("", apiConfig.getUrl());
            assertEquals("", apiConfig.getApiKey());
            assertEquals("embedding-v1", apiConfig.getModel());
        }

        @Test
        @DisplayName("设置API配置属性")
        void testSetApiConfigProperties() {
            // Given
            VectorSearchProperties.ApiConfigProperties apiConfig = new VectorSearchProperties.ApiConfigProperties();
            apiConfig.setUrl("https://api.example.com/embedding");
            apiConfig.setApiKey("sk-test-key-123");
            apiConfig.setModel("text-embedding-ada-002");

            // When
            properties.getEmbedding().setApiConfig(apiConfig);

            // Then
            assertEquals("https://api.example.com/embedding", properties.getEmbedding().getApiConfig().getUrl());
            assertEquals("sk-test-key-123", properties.getEmbedding().getApiConfig().getApiKey());
            assertEquals("text-embedding-ada-002", properties.getEmbedding().getApiConfig().getModel());
        }
    }

    // ==================== 数据库配置测试 ====================

    @Nested
    @DisplayName("数据库配置测试")
    class DatabasePropertiesTests {

        @Test
        @DisplayName("默认数据库配置值")
        void testDefaultDatabaseProperties() {
            // Given
            VectorSearchProperties.DatabaseProperties database = properties.getDatabase();

            // Then
            assertNotNull(database);
            assertEquals("pgvector", database.getType());
            assertNotNull(database.getPgvector());
            assertNotNull(database.getMilvus());
        }

        @Test
        @DisplayName("设置数据库属性")
        void testSetDatabaseProperties() {
            // Given
            VectorSearchProperties.DatabaseProperties database = new VectorSearchProperties.DatabaseProperties();
            database.setType("milvus");

            // When
            properties.setDatabase(database);

            // Then
            assertEquals("milvus", properties.getDatabase().getType());
        }

        @Test
        @DisplayName("数据库类型测试 - pgvector")
        void testDatabaseTypePgvector() {
            // Given
            VectorSearchProperties.DatabaseProperties database = properties.getDatabase();
            database.setType("pgvector");

            // Then
            assertEquals("pgvector", database.getType());
        }

        @Test
        @DisplayName("数据库类型测试 - milvus")
        void testDatabaseTypeMilvus() {
            // Given
            VectorSearchProperties.DatabaseProperties database = properties.getDatabase();
            database.setType("milvus");

            // Then
            assertEquals("milvus", database.getType());
        }
    }

    // ==================== PGVector配置测试 ====================

    @Nested
    @DisplayName("PGVector配置测试")
    class PgVectorPropertiesTests {

        @Test
        @DisplayName("默认PGVector配置值")
        void testDefaultPgVectorProperties() {
            // Given
            VectorSearchProperties.PgVectorProperties pgvector = properties.getDatabase().getPgvector();

            // Then
            assertNotNull(pgvector);
            assertEquals("localhost", pgvector.getHost());
            assertEquals(5432, pgvector.getPort());
            assertEquals("rice_vector", pgvector.getDatabase());
            assertEquals("postgres", pgvector.getUsername());
            assertEquals("", pgvector.getPassword());
        }

        @Test
        @DisplayName("设置PGVector属性")
        void testSetPgVectorProperties() {
            // Given
            VectorSearchProperties.PgVectorProperties pgvector = new VectorSearchProperties.PgVectorProperties();
            pgvector.setHost("192.168.1.100");
            pgvector.setPort(5433);
            pgvector.setDatabase("test_vector");
            pgvector.setUsername("testuser");
            pgvector.setPassword("testpass");

            // When
            properties.getDatabase().setPgvector(pgvector);

            // Then
            assertEquals("192.168.1.100", properties.getDatabase().getPgvector().getHost());
            assertEquals(5433, properties.getDatabase().getPgvector().getPort());
            assertEquals("test_vector", properties.getDatabase().getPgvector().getDatabase());
            assertEquals("testuser", properties.getDatabase().getPgvector().getUsername());
            assertEquals("testpass", properties.getDatabase().getPgvector().getPassword());
        }
    }

    // ==================== Milvus配置测试 ====================

    @Nested
    @DisplayName("Milvus配置测试")
    class MilvusPropertiesTests {

        @Test
        @DisplayName("默认Milvus配置值")
        void testDefaultMilvusProperties() {
            // Given
            VectorSearchProperties.MilvusProperties milvus = properties.getDatabase().getMilvus();

            // Then
            assertNotNull(milvus);
            assertEquals("localhost", milvus.getHost());
            assertEquals(19530, milvus.getPort());
            assertEquals("rice", milvus.getCollectionPrefix());
        }

        @Test
        @DisplayName("设置Milvus属性")
        void testSetMilvusProperties() {
            // Given
            VectorSearchProperties.MilvusProperties milvus = new VectorSearchProperties.MilvusProperties();
            milvus.setHost("192.168.1.200");
            milvus.setPort(19531);
            milvus.setCollectionPrefix("test_prefix");

            // When
            properties.getDatabase().setMilvus(milvus);

            // Then
            assertEquals("192.168.1.200", properties.getDatabase().getMilvus().getHost());
            assertEquals(19531, properties.getDatabase().getMilvus().getPort());
            assertEquals("test_prefix", properties.getDatabase().getMilvus().getCollectionPrefix());
        }
    }

    // ==================== 配置完整性测试 ====================

    @Nested
    @DisplayName("配置完整性测试")
    class ConfigurationIntegrityTests {

        @Test
        @DisplayName("完整配置链路测试")
        void testFullConfigurationChain() {
            // When - 创建完整配置
            VectorSearchProperties fullConfig = new VectorSearchProperties();
            fullConfig.getSearch().setEnabled(true);
            fullConfig.getSearch().setSimilarityThreshold(0.8f);
            fullConfig.getSearch().setMaxResults(5);
            fullConfig.getEmbedding().setModelType("api");
            fullConfig.getEmbedding().getApiConfig().setUrl("https://api.test.com");
            fullConfig.getEmbedding().getApiConfig().setApiKey("test-key");
            fullConfig.getDatabase().setType("milvus");

            // Then
            assertTrue(fullConfig.getSearch().isEnabled());
            assertEquals(0.8f, fullConfig.getSearch().getSimilarityThreshold(), 0.001f);
            assertEquals(5, fullConfig.getSearch().getMaxResults());
            assertEquals("api", fullConfig.getEmbedding().getModelType());
            assertEquals("milvus", fullConfig.getDatabase().getType());
        }

        @Test
        @DisplayName("空配置对象初始化")
        void testEmptyConfigurationInitialization() {
            // When
            VectorSearchProperties newProps = new VectorSearchProperties();

            // Then - 所有嵌套对象应已初始化
            assertNotNull(newProps.getSearch());
            assertNotNull(newProps.getEmbedding());
            assertNotNull(newProps.getDatabase());
            assertNotNull(newProps.getEmbedding().getLocalModel());
            assertNotNull(newProps.getEmbedding().getApiConfig());
            assertNotNull(newProps.getDatabase().getPgvector());
            assertNotNull(newProps.getDatabase().getMilvus());
        }
    }
}
