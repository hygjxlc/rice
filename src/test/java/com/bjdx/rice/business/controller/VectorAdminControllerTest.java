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
 * VectorAdminController 单元测试
 * 测试向量管理控制器的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VectorAdminControllerTest {

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

    // ==================== 状态接口测试 ====================

    @Nested
    @DisplayName("getStatus 状态接口测试")
    class GetStatusTests {

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
    }

    // ==================== 客户向量重建接口测试 ====================

    @Nested
    @DisplayName("rebuildCustomerVectors 客户向量重建测试")
    class RebuildCustomersTests {

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
            doThrow(new RuntimeException("测试异常")).when(vectorSyncService).rebuildAllCustomers();

            // When
            ResponseObj<String> response = controller.rebuildCustomerVectors();

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            assertTrue(response.getMessage().contains("测试异常"));
        }
    }

    // ==================== 商品向量重建接口测试 ====================

    @Nested
    @DisplayName("rebuildProductVectors 商品向量重建测试")
    class RebuildProductsTests {

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

    // ==================== 客户向量检索测试接口 ====================

    @Nested
    @DisplayName("testCustomerSearch 客户检索测试接口")
    class TestCustomerSearchTests {

        @Test
        @DisplayName("测试客户检索 - 找到匹配")
        void testCustomerSearch_Found() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            VectorSearchService.VectorSearchResult mockResult = 
                new VectorSearchService.VectorSearchResult(1L, "测试客户", 0.85f);
            when(vectorSearchService.searchCustomer("测试客户", 3, 0.75f))
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
            when(vectorSearchService.searchCustomer("不存在的客户", 3, 0.75f))
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
        @DisplayName("测试客户检索 - 异常处理")
        void testCustomerSearch_Exception() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(vectorSearchService.searchCustomer(anyString(), anyInt(), anyFloat()))
                    .thenThrow(new RuntimeException("测试异常"));

            // When
            ResponseObj<Map<String, Object>> response = controller.testCustomerSearch("测试客户", 0.75f);

            // Then
            assertNotNull(response);
            assertEquals(2000, response.getCode());
            assertTrue(response.getMessage().contains("测试异常"));
        }
    }

    // ==================== 商品向量检索测试接口 ====================

    @Nested
    @DisplayName("testProductSearch 商品检索测试接口")
    class TestProductSearchTests {

        @Test
        @DisplayName("测试商品检索 - 找到匹配")
        void testProductSearch_Found() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            VectorSearchService.VectorSearchResult mockResult = 
                new VectorSearchService.VectorSearchResult(1L, "测试商品", 0.90f);
            when(vectorSearchService.searchProduct("测试商品", 3, 0.75f))
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
            when(vectorSearchService.searchProduct("不存在的商品", 3, 0.75f))
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
    }

    // ==================== VectorSearchResult 测试 ====================

    @Nested
    @DisplayName("VectorSearchResult 内部类测试")
    class VectorSearchResultTests {

        @Test
        @DisplayName("VectorSearchResult - 成功状态判断")
        void testVectorSearchResult_IsSuccess() {
            // Given
            VectorSearchService.VectorSearchResult result = 
                new VectorSearchService.VectorSearchResult(1L, "测试", 0.85f);

            // When & Then
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getId());
            assertEquals("测试", result.getName());
            assertEquals(0.85f, result.getScore());
        }

        @Test
        @DisplayName("VectorSearchResult - 失败状态判断 - null ID")
        void testVectorSearchResult_IsSuccess_NullId() {
            // Given
            VectorSearchService.VectorSearchResult result = 
                new VectorSearchService.VectorSearchResult(null, "测试", 0.85f);

            // When & Then
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("VectorSearchResult - 失败状态判断 - 零分数")
        void testVectorSearchResult_IsSuccess_ZeroScore() {
            // Given
            VectorSearchService.VectorSearchResult result = 
                new VectorSearchService.VectorSearchResult(1L, "测试", 0.0f);

            // When & Then
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("VectorSearchResult - 设置实体")
        void testVectorSearchResult_SetEntity() {
            // Given
            VectorSearchService.VectorSearchResult result = 
                new VectorSearchService.VectorSearchResult(1L, "测试", 0.85f);
            Object entity = new Object();

            // When
            result.setEntity(entity);

            // Then
            assertEquals(entity, result.getEntity());
        }
    }
}
