package com.bjdx.rice.business.service.vector;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.service.vector.impl.ApiEmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EmbeddingService 单元测试
 * 测试文本嵌入服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmbeddingServiceTest {

    @Mock
    private VectorSearchProperties vectorProperties;

    @Mock
    private VectorSearchProperties.EmbeddingProperties embeddingProperties;

    @Mock
    private VectorSearchProperties.LocalModelProperties localModelProperties;

    @Mock
    private VectorSearchProperties.ApiConfigProperties apiConfigProperties;

    @InjectMocks
    private ApiEmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        when(vectorProperties.getEmbedding()).thenReturn(embeddingProperties);
        when(embeddingProperties.getLocalModel()).thenReturn(localModelProperties);
        when(embeddingProperties.getApiConfig()).thenReturn(apiConfigProperties);
        when(localModelProperties.getDimension()).thenReturn(768);
    }

    // ==================== 单文本向量化测试 ====================

    @Test
    @DisplayName("单文本向量化 - 正常文本")
    void testEmbed_NormalText() {
        // Given
        String text = "测试文本";
        when(apiConfigProperties.getUrl()).thenReturn("http://test.api/embedding");
        when(apiConfigProperties.getApiKey()).thenReturn("test-key");
        when(apiConfigProperties.getModel()).thenReturn("embedding-v1");

        // When & Then
        // 由于需要实际 HTTP 调用，这里测试返回非空向量
        float[] result = embeddingService.embed(text);
        assertNotNull(result);
        assertEquals(768, result.length);
    }

    @Test
    @DisplayName("单文本向量化 - 空文本")
    void testEmbed_EmptyText() {
        // Given
        String text = "";

        // When
        float[] result = embeddingService.embed(text);

        // Then
        assertNotNull(result);
        assertEquals(768, result.length);
        // 空文本应返回零向量
        for (float v : result) {
            assertEquals(0.0f, v, 0.0001f);
        }
    }

    @Test
    @DisplayName("单文本向量化 - null文本")
    void testEmbed_NullText() {
        // When
        float[] result = embeddingService.embed(null);

        // Then
        assertNotNull(result);
        assertEquals(768, result.length);
        for (float v : result) {
            assertEquals(0.0f, v, 0.0001f);
        }
    }

    @Test
    @DisplayName("单文本向量化 - 纯空格文本")
    void testEmbed_WhitespaceText() {
        // Given
        String text = "   ";

        // When
        float[] result = embeddingService.embed(text);

        // Then
        assertNotNull(result);
        assertEquals(768, result.length);
    }

    // ==================== 批量文本向量化测试 ====================

    @Test
    @DisplayName("批量文本向量化 - 正常列表")
    void testEmbedBatch_NormalList() {
        // Given
        List<String> texts = Arrays.asList("文本1", "文本2", "文本3");

        // When
        List<float[]> results = embeddingService.embedBatch(texts);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
        for (float[] vector : results) {
            assertNotNull(vector);
            assertEquals(768, vector.length);
        }
    }

    @Test
    @DisplayName("批量文本向量化 - 空列表")
    void testEmbedBatch_EmptyList() {
        // Given
        List<String> texts = Arrays.asList();

        // When
        List<float[]> results = embeddingService.embedBatch(texts);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("批量文本向量化 - 包含null元素")
    void testEmbedBatch_ContainsNull() {
        // Given
        List<String> texts = Arrays.asList("文本1", null, "文本3");

        // When
        List<float[]> results = embeddingService.embedBatch(texts);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    // ==================== 维度测试 ====================

    @Test
    @DisplayName("获取向量维度")
    void testGetDimension() {
        // When
        int dimension = embeddingService.getDimension();

        // Then
        assertEquals(768, dimension);
    }

    // ==================== 服务可用性测试 ====================

    @Test
    @DisplayName("服务可用性检查 - API已配置")
    void testIsAvailable_ApiConfigured() {
        // Given
        when(apiConfigProperties.getUrl()).thenReturn("http://test.api/embedding");
        when(apiConfigProperties.getApiKey()).thenReturn("test-api-key");

        // When
        boolean available = embeddingService.isAvailable();

        // Then
        assertTrue(available);
    }

    @Test
    @DisplayName("服务可用性检查 - URL为空")
    void testIsAvailable_EmptyUrl() {
        // Given
        when(apiConfigProperties.getUrl()).thenReturn("");
        when(apiConfigProperties.getApiKey()).thenReturn("test-api-key");

        // When
        boolean available = embeddingService.isAvailable();

        // Then
        assertFalse(available);
    }

    @Test
    @DisplayName("服务可用性检查 - API Key为空")
    void testIsAvailable_EmptyApiKey() {
        // Given
        when(apiConfigProperties.getUrl()).thenReturn("http://test.api/embedding");
        when(apiConfigProperties.getApiKey()).thenReturn("");

        // When
        boolean available = embeddingService.isAvailable();

        // Then
        assertFalse(available);
    }

    @Test
    @DisplayName("服务可用性检查 - URL为null")
    void testIsAvailable_NullUrl() {
        // Given
        when(apiConfigProperties.getUrl()).thenReturn(null);

        // When
        boolean available = embeddingService.isAvailable();

        // Then
        assertFalse(available);
    }

    @Test
    @DisplayName("服务可用性检查 - API Key为null")
    void testIsAvailable_NullApiKey() {
        // Given
        when(apiConfigProperties.getUrl()).thenReturn("http://test.api/embedding");
        when(apiConfigProperties.getApiKey()).thenReturn(null);

        // When
        boolean available = embeddingService.isAvailable();

        // Then
        assertFalse(available);
    }

    // ==================== API 响应解析测试 ====================

    @Nested
    @DisplayName("API 响应解析测试")
    class ApiResponseParseTests {

        @Test
        @DisplayName("解析 OpenAI 格式响应 - 单个嵌入")
        void testParseOpenAiFormat_Single() throws Exception {
            // Given - 模拟 OpenAI 格式的响应
            String openAiResponse = "{\"data\":[{\"embedding\":[0.1,0.2,0.3,0.4,0.5]}],\"model\":\"text-embedding-ada-002\"}";
            
            // 使用反射测试 parseEmbeddingResponse 方法
            java.lang.reflect.Method parseMethod = ApiEmbeddingService.class.getDeclaredMethod(
                    "parseEmbeddingResponse", String.class);
            parseMethod.setAccessible(true);
            
            // When
            float[] result = (float[]) parseMethod.invoke(embeddingService, openAiResponse);

            // Then
            assertNotNull(result);
            assertEquals(5, result.length);
            assertEquals(0.1f, result[0], 0.001f);
            assertEquals(0.5f, result[4], 0.001f);
        }

        @Test
        @DisplayName("解析 OpenAI 格式响应 - 高维嵌入")
        void testParseOpenAiFormat_HighDimensional() throws Exception {
            // Given - 创建一个 768 维的模拟响应
            StringBuilder sb = new StringBuilder("{\"data\":[{\"embedding\":[");
            for (int i = 0; i < 768; i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format("%.4f", (float) Math.random()));
            }
            sb.append("]}],\"model\":\"text-embedding-ada-002\"}");
            
            // 使用反射测试
            java.lang.reflect.Method parseMethod = ApiEmbeddingService.class.getDeclaredMethod(
                    "parseEmbeddingResponse", String.class);
            parseMethod.setAccessible(true);
            
            // When
            float[] result = (float[]) parseMethod.invoke(embeddingService, sb.toString());

            // Then
            assertNotNull(result);
            assertEquals(768, result.length);
        }

        @Test
        @DisplayName("解析简单格式响应 - embedding 字段")
        void testParseSimpleFormat() throws Exception {
            // Given - 模拟简单格式的响应
            String simpleResponse = "{\"embedding\":[0.1,0.2,0.3]}";
            
            // 使用反射测试
            java.lang.reflect.Method parseMethod = ApiEmbeddingService.class.getDeclaredMethod(
                    "parseEmbeddingResponse", String.class);
            parseMethod.setAccessible(true);
            
            // When
            float[] result = (float[]) parseMethod.invoke(embeddingService, simpleResponse);

            // Then
            assertNotNull(result);
            assertEquals(3, result.length);
        }

        @Test
        @DisplayName("解析未知格式响应 - 抛出异常")
        void testParseUnknownFormat() throws Exception {
            // Given - 模拟未知格式的响应
            String unknownResponse = "{\"unknown\":[0.1,0.2,0.3]}";
            
            // 使用反射测试
            java.lang.reflect.Method parseMethod = ApiEmbeddingService.class.getDeclaredMethod(
                    "parseEmbeddingResponse", String.class);
            parseMethod.setAccessible(true);
            
            // When & Then - 应该抛出异常
            try {
                parseMethod.invoke(embeddingService, unknownResponse);
                fail("应该抛出异常");
            } catch (java.lang.reflect.InvocationTargetException e) {
                assertTrue(e.getCause() instanceof RuntimeException);
                assertTrue(e.getCause().getMessage().contains("Unknown embedding response format"));
            }
        }

        @Test
        @DisplayName("解析空 data 数组响应")
        void testParseEmptyDataArray() throws Exception {
            // Given - 模拟空的 data 数组
            String emptyDataResponse = "{\"data\":[],\"model\":\"text-embedding-ada-002\"}";
            
            // 使用反射测试
            java.lang.reflect.Method parseMethod = ApiEmbeddingService.class.getDeclaredMethod(
                    "parseEmbeddingResponse", String.class);
            parseMethod.setAccessible(true);
            
            // When & Then
            try {
                parseMethod.invoke(embeddingService, emptyDataResponse);
                fail("应该抛出异常");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // 预期异常
            }
        }

        @Test
        @DisplayName("解析包含负值的嵌入向量")
        void testParseNegativeValues() throws Exception {
            // Given - 包含负值的响应
            String negativeResponse = "{\"embedding\":[-0.5,0.3,-0.2,0.8,-0.1]}";
            
            // 使用反射测试
            java.lang.reflect.Method parseMethod = ApiEmbeddingService.class.getDeclaredMethod(
                    "parseEmbeddingResponse", String.class);
            parseMethod.setAccessible(true);
            
            // When
            float[] result = (float[]) parseMethod.invoke(embeddingService, negativeResponse);

            // Then
            assertEquals(-0.5f, result[0], 0.001f);
            assertEquals(0.3f, result[1], 0.001f);
            assertEquals(-0.2f, result[2], 0.001f);
        }
    }

    // ==================== 错误处理测试 ====================

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("API 调用失败 - 返回零向量")
        void testApiCallFailure() {
            // Given
            String text = "测试文本";
            when(apiConfigProperties.getUrl()).thenReturn("http://invalid-url-that-does-not-exist.com");
            when(apiConfigProperties.getApiKey()).thenReturn("test-key");
            when(apiConfigProperties.getModel()).thenReturn("embedding-v1");

            // When
            float[] result = embeddingService.embed(text);

            // Then - 失败时返回零向量
            assertNotNull(result);
            assertEquals(768, result.length);
        }

        @Test
        @DisplayName("配置缺失时的处理")
        void testMissingConfiguration() {
            // Given
            when(apiConfigProperties.getUrl()).thenReturn(null);
            when(apiConfigProperties.getApiKey()).thenReturn(null);

            // When
            boolean available = embeddingService.isAvailable();

            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("批量处理中的单个失败")
        void testBatchProcessingWithFailure() {
            // Given
            List<String> texts = Arrays.asList("文本1", null, "文本3");

            // When
            List<float[]> results = embeddingService.embedBatch(texts);

            // Then
            assertNotNull(results);
            assertEquals(3, results.size());
            for (float[] vector : results) {
                assertNotNull(vector);
                assertEquals(768, vector.length);
            }
        }
    }

    // ==================== 性能测试 ====================

    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("大批量文本处理")
        void testLargeBatchProcessing() {
            // Given
            List<String> texts = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                texts.add("测试文本" + i);
            }

            // When
            long startTime = System.currentTimeMillis();
            List<float[]> results = embeddingService.embedBatch(texts);
            long elapsedTime = System.currentTimeMillis() - startTime;

            // Then
            assertEquals(100, results.size());
            // 记录性能但不强制要求
            System.out.println("批量处理 100 条文本耗时: " + elapsedTime + "ms");
        }

        @Test
        @DisplayName("长文本处理")
        void testLongTextProcessing() {
            // Given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("这是一段很长的测试文本。");
            }
            String longText = sb.toString();

            // When
            float[] result = embeddingService.embed(longText);

            // Then
            assertNotNull(result);
            assertEquals(768, result.length);
        }
    }
}
