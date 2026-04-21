package com.bjdx.rice.business.service.vector;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.CustomerVector;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.entity.ProductVector;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.bjdx.rice.business.mapper.CustomerVectorMapper;
import com.bjdx.rice.business.mapper.ProductMapper;
import com.bjdx.rice.business.mapper.ProductVectorMapper;
import com.bjdx.rice.business.service.vector.impl.VectorSearchServiceImpl;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * VectorSearchService 单元测试
 * 测试向量检索服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VectorSearchServiceTest {

    @Mock
    private VectorSearchProperties vectorProperties;

    @Mock
    private VectorSearchProperties.SearchProperties searchProperties;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private CustomerVectorMapper customerVectorMapper;

    @Mock
    private ProductVectorMapper productVectorMapper;

    @Mock
    private CustomerInfoMapper customerInfoMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private VectorSearchServiceImpl vectorSearchService;

    private float[] testVector;

    @BeforeEach
    void setUp() {
        // 初始化测试向量（768维）
        testVector = new float[768];
        for (int i = 0; i < 768; i++) {
            testVector[i] = (float) Math.random();
        }

        when(vectorProperties.getSearch()).thenReturn(searchProperties);
    }

    // ==================== 客户向量搜索测试 ====================

    @Nested
    @DisplayName("客户向量搜索测试")
    class CustomerSearchTests {

        @Test
        @DisplayName("搜索客户 - 成功匹配")
        void testSearchCustomer_Success() {
            // Given
            String customerName = "测试客户";
            int topK = 3;
            float threshold = 0.75f;

            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(customerName)).thenReturn(testVector);

            CustomerVector customerVector = createTestCustomerVector(1L, "测试客户", testVector);
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(customerVector));

            CustomerInfo customerInfo = createTestCustomerInfo(1L, "测试客户");
            when(customerInfoMapper.selectByPrimaryKey(1L)).thenReturn(customerInfo);

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    customerName, topK, threshold);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getId());
            assertEquals("测试客户", result.getName());
            assertTrue(result.getScore() > 0);
            assertTrue(result.getScore() <= 1.0f);
        }

        @Test
        @DisplayName("搜索客户 - 无匹配结果")
        void testSearchCustomer_NoMatch() {
            // Given
            String customerName = "不存在的客户";
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(customerName)).thenReturn(testVector);
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.emptyList());

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    customerName, 3, 0.75f);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("搜索客户 - 服务不可用")
        void testSearchCustomer_ServiceNotAvailable() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    "测试客户", 3, 0.75f);

            // Then
            assertNull(result);
            verify(embeddingService, never()).embed(any());
        }

        @Test
        @DisplayName("搜索客户 - 向量映射返回但客户信息不存在")
        void testSearchCustomer_VectorFoundButCustomerNotFound() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(any())).thenReturn(testVector);

            CustomerVector customerVector = createTestCustomerVector(999L, "客户", testVector);
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(customerVector));
            when(customerInfoMapper.selectByPrimaryKey(999L)).thenReturn(null);

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    "测试客户", 3, 0.75f);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("搜索客户 - 空客户名称")
        void testSearchCustomer_EmptyName() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed("")).thenReturn(new float[768]);
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.emptyList());

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    "", 3, 0.75f);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("搜索客户 - 异常处理")
        void testSearchCustomer_ExceptionHandling() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(any())).thenThrow(new RuntimeException("测试异常"));

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    "测试客户", 3, 0.75f);

            // Then
            assertNull(result);
        }
    }

    // ==================== 商品向量搜索测试 ====================

    @Nested
    @DisplayName("商品向量搜索测试")
    class ProductSearchTests {

        @Test
        @DisplayName("搜索商品 - 成功匹配")
        void testSearchProduct_Success() {
            // Given
            String productName = "测试商品";
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(productName)).thenReturn(testVector);

            ProductVector productVector = createTestProductVector(1L, "测试商品", testVector);
            when(productVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(productVector));

            Product product = createTestProduct(1L, "测试商品");
            when(productMapper.selectByPrimaryKey(1L)).thenReturn(product);

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchProduct(
                    productName, 3, 0.75f);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getId());
            assertEquals("测试商品", result.getName());
        }

        @Test
        @DisplayName("搜索商品 - 无匹配结果")
        void testSearchProduct_NoMatch() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(any())).thenReturn(testVector);
            when(productVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.emptyList());

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchProduct(
                    "不存在商品", 3, 0.75f);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("搜索商品 - 服务不可用")
        void testSearchProduct_ServiceNotAvailable() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchProduct(
                    "测试商品", 3, 0.75f);

            // Then
            assertNull(result);
        }
    }

    // ==================== 服务可用性测试 ====================

    @Nested
    @DisplayName("服务可用性测试")
    class AvailabilityTests {

        @Test
        @DisplayName("服务可用 - 全部条件满足")
        void testIsAvailable_AllConditionsMet() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);

            // When
            boolean available = vectorSearchService.isAvailable();

            // Then
            assertTrue(available);
        }

        @Test
        @DisplayName("服务不可用 - 功能未启用")
        void testIsAvailable_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            boolean available = vectorSearchService.isAvailable();

            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("服务不可用 - 嵌入服务不可用")
        void testIsAvailable_EmbeddingServiceNotAvailable() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(false);

            // When
            boolean available = vectorSearchService.isAvailable();

            // Then
            assertFalse(available);
        }
    }

    // ==================== 相似度阈值测试 ====================

    @Nested
    @DisplayName("相似度阈值测试")
    class ThresholdTests {

        @Test
        @DisplayName("高阈值 - 只返回非常相似的结果")
        void testHighThreshold() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(any())).thenReturn(testVector);
            
            // 模拟低相似度结果
            CustomerVector lowSimilarityVector = createTestCustomerVector(1L, "客户", createLowSimilarityVector());
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.emptyList()); // 高阈值下无匹配

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    "测试客户", 3, 0.95f);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("低阈值 - 返回更多结果")
        void testLowThreshold() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            when(embeddingService.isAvailable()).thenReturn(true);
            when(embeddingService.embed(any())).thenReturn(testVector);

            CustomerVector customerVector = createTestCustomerVector(1L, "客户", testVector);
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(customerVector));
            when(customerInfoMapper.selectByPrimaryKey(1L)).thenReturn(createTestCustomerInfo(1L, "客户"));

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    "测试客户", 3, 0.5f);

            // Then
            assertNotNull(result);
        }
    }

    // ==================== 辅助方法 ====================

    private CustomerVector createTestCustomerVector(Long id, String name, float[] vector) {
        CustomerVector cv = new CustomerVector();
        cv.setId(id);
        cv.setUnitName(name);
        cv.setVector(vector);
        return cv;
    }

    private ProductVector createTestProductVector(Long id, String name, float[] vector) {
        ProductVector pv = new ProductVector();
        pv.setId(id);
        pv.setProductName(name);
        pv.setVector(vector);
        return pv;
    }

    private CustomerInfo createTestCustomerInfo(Long id, String name) {
        CustomerInfo ci = new CustomerInfo();
        ci.setId(id);
        ci.setUnitName(name);
        return ci;
    }

    private Product createTestProduct(Long id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        return p;
    }

    private float[] createLowSimilarityVector() {
        float[] vector = new float[768];
        // 创建一个低相似度的向量
        for (int i = 0; i < 768; i++) {
            vector[i] = (float) (Math.random() * 0.1); // 低值
        }
        return vector;
    }
}
