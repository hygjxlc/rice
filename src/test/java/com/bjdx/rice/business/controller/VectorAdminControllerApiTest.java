package com.bjdx.rice.business.controller;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.dto.ResponseObj;
import com.bjdx.rice.business.service.vector.VectorSearchService;
import com.bjdx.rice.business.service.vector.VectorSyncService;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VectorAdminController 接口测试
 * 测试向量管理控制器的 API 行为
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VectorAdminControllerApiTest {

    @Mock
    private VectorSearchProperties vectorProperties;

    @Mock
    private VectorSearchProperties.SearchProperties searchProperties;

    @Mock
    private VectorSearchProperties.EmbeddingProperties embeddingProperties;

    @Mock
    private VectorSearchProperties.DatabaseProperties databaseProperties;

    @Mock
    private VectorSearchService vectorSearchService;

    @Mock
    private VectorSyncService vectorSyncService;

    @InjectMocks
    private VectorAdminController controller;

    @BeforeEach
    void setUp() {
        when(vectorProperties.getSearch()).thenReturn(searchProperties);
        when(vectorProperties.getEmbedding()).thenReturn(embeddingProperties);
        when(vectorProperties.getDatabase()).thenReturn(databaseProperties);
    }

    // ==================== GET /status 接口测试 ====================

    @Nested
    @DisplayName("GET /status 状态接口测试")
    class GetStatusApiTests {

        @Test
        @DisplayName("获取状态 - 功能启用")
        void testGetStatus_Enabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(searchProperties.getSimilarityThreshold()).thenReturn(0.75f);
            when(searchProperties.getMaxResults()).thenReturn(3);
            when(searchProperties.isLogMatches()).thenReturn(true);
            when(embeddingProperties.getModelType()).thenReturn("local");
            when(databaseProperties.getType()).thenReturn("pgvector");
            when(vectorSearchService.isAvailable()).thenReturn(true);

            // When
            ResponseObj<Map<String, Object>> response = controller.getStatus();

            // Then
            assertNotNull(response);
            assertEquals(1000, response.getCode());
            Map<String, Object> data = response.getData();
            assertEquals(true, data.get("enabled"));
            assertEquals(0.75f, data.get("similarityThreshold"));
            assertEquals(3, data.get("maxResults"));
            assertEquals(true, data.get("logMatches"));
            assertEquals("local", data.get("modelType"));
            assertEquals("pgvector", data.get("databaseType"));
            assertEquals(true, data.get("available"));
        }

        @Test
        @DisplayName("获取状态 - 功能禁用")
        void testGetStatus_Disabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);
            when(searchProperties.getSimilarityThreshold()).thenReturn(0.75f);
            when(searchProperties.getMaxResults()).thenReturn(3);
            when(searchProperties.isLogMatches()).thenReturn(false);
            when(embeddingProperties.getModelType()).thenReturn("api");
            when(databaseProperties.getType()).thenReturn("milvus");
            when(vectorSearchService.isAvailable()).thenReturn(false);

            // When
            ResponseObj<Map<String, Object>> response = controller.getStatus();

            // Then
            assertNotNull(response);
            Map<String, Object> data = response.getData();
            assertEquals(false, data.get("enabled"));
            assertEquals(false, data.get("available"));
        }

        @Test
        @DisplayName("获取状态 - 验证所有配置字段")
        void testGetStatus_AllFieldsPresent() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(searchProperties.getSimilarityThreshold()).thenReturn(0.8f);
            when(searchProperties.getMaxResults()).thenReturn(5);
            when(searchProperties.isLogMatches()).thenReturn(false);
            when(embeddingProperties.getModelType()).thenReturn("api");
            when(databaseProperties.getType()).thenReturn("milvus");
            when(vectorSearchService.isAvailable()).thenReturn(true);

            // When
            ResponseObj<Map<String, Object>> response = controller.getStatus();

            // Then
            Map<String, Object> data = response.getData();
            assertTrue(data.containsKey("enabled"));
            assertTrue(data.containsKey("similarityThreshold"));
            assertTrue(data.containsKey("maxResults"));
            assertTrue(data.containsKey("logMatches"));
            assertTrue(data.containsKey("modelType"));
            assertTrue(data.containsKey("databaseType"));
            assertTrue(data.containsKey("available"));
        }
    }

    // ==================== POST /rebuild/customers 接口测试 ====================

    @Nested
    @DisplayName("POST /rebuild/customers 接口测试")
    class RebuildCustomersApiTests {

        @Test
        @DisplayName("重建客户向量 - 成功")
        void testRebuildCustomerVectors_Success() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            doNothing().when(vectorSyncService).rebuildAllCustomers();

            // When
            ResponseObj<String> response = controller.rebuildCustomerVectors();

            // Then
            assertNotNull(response);
            assertEquals(1000, response.getCode());
            assertEquals("客户向量重建任务已启动", response.getData());
            verify(vectorSyncService).rebuildAllCustomers();
        }

        @Test
        @DisplayName("重建客户向量 - 功能未启用")
        void testRebuildCustomerVectors_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            ResponseObj<String> response = controller.rebuildCustomerVectors();

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            verify(vectorSyncService, never()).rebuildAllCustomers();
        }

        @Test
        @DisplayName("重建客户向量 - 异常处理")
        void testRebuildCustomerVectors_Exception() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            doThrow(new RuntimeException("数据库连接失败")).when(vectorSyncService).rebuildAllCustomers();

            // When
            ResponseObj<String> response = controller.rebuildCustomerVectors();

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            assertTrue(response.getMessage().contains("数据库连接失败"));
        }
    }

    // ==================== POST /rebuild/products 接口测试 ====================

    @Nested
    @DisplayName("POST /rebuild/products 接口测试")
    class RebuildProductsApiTests {

        @Test
        @DisplayName("重建商品向量 - 成功")
        void testRebuildProductVectors_Success() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            doNothing().when(vectorSyncService).rebuildAllProducts();

            // When
            ResponseObj<String> response = controller.rebuildProductVectors();

            // Then
            assertNotNull(response);
            assertEquals(1000, response.getCode());
            assertEquals("商品向量重建任务已启动", response.getData());
            verify(vectorSyncService).rebuildAllProducts();
        }

        @Test
        @DisplayName("重建商品向量 - 功能未启用")
        void testRebuildProductVectors_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            ResponseObj<String> response = controller.rebuildProductVectors();

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            verify(vectorSyncService, never()).rebuildAllProducts();
        }
    }

    // ==================== GET /test/customer 接口测试 ====================

    @Nested
    @DisplayName("GET /test/customer 接口测试")
    class TestCustomerSearchApiTests {

        @Test
        @DisplayName("测试客户检索 - 找到匹配")
        void testCustomerSearch_Found() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            VectorSearchService.VectorSearchResult mockResult = 
                    new VectorSearchService.VectorSearchResult(1L, "测试客户", 0.85f);
            when(vectorSearchService.searchCustomer(eq("测试客户"), anyInt(), anyFloat()))
                    .thenReturn(mockResult);

            // When
            ResponseObj<Map<String, Object>> response = controller.testCustomerSearch("测试客户", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(1000, response.getCode());
            Map<String, Object> data = response.getData();
            assertEquals(true, data.get("found"));
            assertEquals(1L, data.get("id"));
            assertEquals("测试客户", data.get("name"));
            assertEquals(0.85f, data.get("score"));
        }

        @Test
        @DisplayName("测试客户检索 - 未找到匹配")
        void testCustomerSearch_NotFound() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(vectorSearchService.searchCustomer(anyString(), anyInt(), anyFloat()))
                    .thenReturn(null);

            // When
            ResponseObj<Map<String, Object>> response = controller.testCustomerSearch("不存在的客户", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(1000, response.getCode());
            assertEquals(false, response.getData().get("found"));
        }

        @Test
        @DisplayName("测试客户检索 - 功能未启用")
        void testCustomerSearch_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            ResponseObj<Map<String, Object>> response = controller.testCustomerSearch("测试客户", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            verify(vectorSearchService, never()).searchCustomer(anyString(), anyInt(), anyFloat());
        }

        @Test
        @DisplayName("测试客户检索 - 不同阈值参数")
        void testCustomerSearch_DifferentThresholds() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            VectorSearchService.VectorSearchResult mockResult = 
                    new VectorSearchService.VectorSearchResult(1L, "测试客户", 0.9f);
            when(vectorSearchService.searchCustomer(anyString(), anyInt(), anyFloat()))
                    .thenReturn(mockResult);

            // When - 测试不同阈值
            ResponseObj<Map<String, Object>> response1 = controller.testCustomerSearch("测试", 0.5f);
            ResponseObj<Map<String, Object>> response2 = controller.testCustomerSearch("测试", 0.9f);

            // Then
            assertEquals(1000, response1.getCode());
            assertEquals(1000, response2.getCode());
            assertEquals(true, response1.getData().get("found"));
            assertEquals(true, response2.getData().get("found"));
        }

        @Test
        @DisplayName("测试客户检索 - 异常处理")
        void testCustomerSearch_Exception() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(vectorSearchService.searchCustomer(anyString(), anyInt(), anyFloat()))
                    .thenThrow(new RuntimeException("搜索服务异常"));

            // When
            ResponseObj<Map<String, Object>> response = controller.testCustomerSearch("测试客户", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            assertTrue(response.getMessage().contains("搜索服务异常"));
        }
    }

    // ==================== GET /test/product 接口测试 ====================

    @Nested
    @DisplayName("GET /test/product 接口测试")
    class TestProductSearchApiTests {

        @Test
        @DisplayName("测试商品检索 - 找到匹配")
        void testProductSearch_Found() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            VectorSearchService.VectorSearchResult mockResult = 
                    new VectorSearchService.VectorSearchResult(1L, "测试商品", 0.90f);
            when(vectorSearchService.searchProduct(eq("测试商品"), anyInt(), anyFloat()))
                    .thenReturn(mockResult);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("测试商品", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(1000, response.getCode());
            Map<String, Object> data = response.getData();
            assertEquals(true, data.get("found"));
            assertEquals(1L, data.get("id"));
            assertEquals("测试商品", data.get("name"));
            assertEquals(0.90f, data.get("score"));
        }

        @Test
        @DisplayName("测试商品检索 - 未找到匹配")
        void testProductSearch_NotFound() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(vectorSearchService.searchProduct(anyString(), anyInt(), anyFloat()))
                    .thenReturn(null);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("不存在的商品", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(1000, response.getCode());
            assertEquals(false, response.getData().get("found"));
        }

        @Test
        @DisplayName("测试商品检索 - 功能未启用")
        void testProductSearch_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("测试商品", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            verify(vectorSearchService, never()).searchProduct(anyString(), anyInt(), anyFloat());
        }

        @Test
        @DisplayName("测试商品检索 - 高相似度结果")
        void testProductSearch_HighSimilarity() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            VectorSearchService.VectorSearchResult mockResult = 
                    new VectorSearchService.VectorSearchResult(1L, "精确匹配商品", 0.99f);
            when(vectorSearchService.searchProduct(anyString(), anyInt(), anyFloat()))
                    .thenReturn(mockResult);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("测试商品", 0.95f);

            // Then
            assertEquals(true, response.getData().get("found"));
            assertEquals(0.99f, response.getData().get("score"));
        }

        @Test
        @DisplayName("测试商品检索 - 低相似度结果")
        void testProductSearch_LowSimilarity() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            VectorSearchService.VectorSearchResult mockResult = 
                    new VectorSearchService.VectorSearchResult(1L, "部分匹配商品", 0.6f);
            when(vectorSearchService.searchProduct(anyString(), anyInt(), anyFloat()))
                    .thenReturn(mockResult);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("测试商品", 0.5f);

            // Then
            assertEquals(true, response.getData().get("found"));
            assertEquals(0.6f, response.getData().get("score"));
        }
    }

    // ==================== 边界和异常测试 ====================

    @Nested
    @DisplayName("边界和异常测试")
    class BoundaryAndExceptionTests {

        @Test
        @DisplayName("空名称客户检索")
        void testCustomerSearch_EmptyName() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(vectorSearchService.searchCustomer(eq(""), anyInt(), anyFloat()))
                    .thenReturn(null);

            // When
            ResponseObj<Map<String, Object>> response = controller.testCustomerSearch("", 0.75f);

            // Then
            assertEquals(false, response.getData().get("found"));
        }

        @Test
        @DisplayName("空名称商品检索")
        void testProductSearch_EmptyName() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(vectorSearchService.searchProduct(eq(""), anyInt(), anyFloat()))
                    .thenReturn(null);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("", 0.75f);

            // Then
            assertEquals(false, response.getData().get("found"));
        }

        @Test
        @DisplayName("阈值边界 - 最小值")
        void testThreshold_Minimum() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            VectorSearchService.VectorSearchResult mockResult = 
                    new VectorSearchService.VectorSearchResult(1L, "测试", 0.1f);
            when(vectorSearchService.searchProduct(anyString(), anyInt(), anyFloat()))
                    .thenReturn(mockResult);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("测试", 0.0f);

            // Then
            assertEquals(1000, response.getCode());
        }

        @Test
        @DisplayName("阈值边界 - 最大值")
        void testThreshold_Maximum() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(vectorSearchService.searchProduct(anyString(), anyInt(), eq(1.0f)))
                    .thenReturn(null);

            // When
            ResponseObj<Map<String, Object>> response = controller.testProductSearch("测试", 1.0f);

            // Then
            assertEquals(1000, response.getCode());
            assertEquals(false, response.getData().get("found"));
        }
    }
}
