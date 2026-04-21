package com.bjdx.rice.business.service.vector;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.CustomerVector;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.entity.ProductVector;
import com.bjdx.rice.business.mapper.CustomerVectorMapper;
import com.bjdx.rice.business.mapper.ProductVectorMapper;
import com.bjdx.rice.business.service.vector.impl.VectorSyncServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * VectorSyncService 单元测试
 * 测试向量同步服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VectorSyncServiceTest {

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

    @InjectMocks
    private VectorSyncServiceImpl vectorSyncService;

    private float[] testVector;

    @BeforeEach
    void setUp() {
        testVector = new float[768];
        for (int i = 0; i < 768; i++) {
            testVector[i] = (float) Math.random();
        }

        when(vectorProperties.getSearch()).thenReturn(searchProperties);
    }

    // ==================== 单客户同步测试 ====================

    @Nested
    @DisplayName("单客户同步测试")
    class SyncCustomerTests {

        @Test
        @DisplayName("同步客户 - 成功")
        void testSyncCustomer_Success() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            CustomerInfo customer = createTestCustomerInfo(1L, "测试客户", "别名");
            when(embeddingService.embed("测试客户 别名")).thenReturn(testVector);
            when(customerVectorMapper.upsert(any(CustomerVector.class))).thenReturn(1);

            // When
            vectorSyncService.syncCustomer(customer);

            // Then
            verify(embeddingService).embed("测试客户 别名");
            verify(customerVectorMapper).upsert(any(CustomerVector.class));
        }

        @Test
        @DisplayName("同步客户 - 无别名")
        void testSyncCustomer_NoAlias() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            CustomerInfo customer = createTestCustomerInfo(1L, "测试客户", null);
            when(embeddingService.embed("测试客户")).thenReturn(testVector);

            // When
            vectorSyncService.syncCustomer(customer);

            // Then
            verify(embeddingService).embed("测试客户");
            verify(customerVectorMapper).upsert(any(CustomerVector.class));
        }

        @Test
        @DisplayName("同步客户 - 空别名")
        void testSyncCustomer_EmptyAlias() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            CustomerInfo customer = createTestCustomerInfo(1L, "测试客户", "");
            when(embeddingService.embed("测试客户")).thenReturn(testVector);

            // When
            vectorSyncService.syncCustomer(customer);

            // Then
            verify(embeddingService).embed("测试客户");
        }

        @Test
        @DisplayName("同步客户 - 功能未启用")
        void testSyncCustomer_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);
            CustomerInfo customer = createTestCustomerInfo(1L, "测试客户", null);

            // When
            vectorSyncService.syncCustomer(customer);

            // Then
            verify(embeddingService, never()).embed(any());
            verify(customerVectorMapper, never()).upsert(any());
        }

        @Test
        @DisplayName("同步客户 - null客户")
        void testSyncCustomer_NullCustomer() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);

            // When
            vectorSyncService.syncCustomer(null);

            // Then
            verify(embeddingService, never()).embed(any());
            verify(customerVectorMapper, never()).upsert(any());
        }

        @Test
        @DisplayName("同步客户 - 异常处理")
        void testSyncCustomer_ExceptionHandling() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            CustomerInfo customer = createTestCustomerInfo(1L, "测试客户", null);
            when(embeddingService.embed(any())).thenThrow(new RuntimeException("测试异常"));

            // When & Then - 不应抛出异常
            assertDoesNotThrow(() -> vectorSyncService.syncCustomer(customer));
        }
    }

    // ==================== 单商品同步测试 ====================

    @Nested
    @DisplayName("单商品同步测试")
    class SyncProductTests {

        @Test
        @DisplayName("同步商品 - 成功")
        void testSyncProduct_Success() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            Product product = createTestProduct(1L, "测试商品", "PROD001");
            when(embeddingService.embed("测试商品 PROD001")).thenReturn(testVector);
            when(productVectorMapper.upsert(any(ProductVector.class))).thenReturn(1);

            // When
            vectorSyncService.syncProduct(product);

            // Then
            verify(embeddingService).embed("测试商品 PROD001");
            verify(productVectorMapper).upsert(any(ProductVector.class));
        }

        @Test
        @DisplayName("同步商品 - 无编码")
        void testSyncProduct_NoCode() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            Product product = createTestProduct(1L, "测试商品", null);
            when(embeddingService.embed("测试商品")).thenReturn(testVector);

            // When
            vectorSyncService.syncProduct(product);

            // Then
            verify(embeddingService).embed("测试商品");
        }

        @Test
        @DisplayName("同步商品 - 功能未启用")
        void testSyncProduct_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);
            Product product = createTestProduct(1L, "测试商品", null);

            // When
            vectorSyncService.syncProduct(product);

            // Then
            verify(embeddingService, never()).embed(any());
            verify(productVectorMapper, never()).upsert(any());
        }

        @Test
        @DisplayName("同步商品 - null商品")
        void testSyncProduct_NullProduct() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);

            // When
            vectorSyncService.syncProduct(null);

            // Then
            verify(embeddingService, never()).embed(any());
            verify(productVectorMapper, never()).upsert(any());
        }
    }

    // ==================== 批量异步同步测试 ====================

    @Nested
    @DisplayName("批量异步同步测试")
    class AsyncSyncTests {

        @Test
        @DisplayName("异步同步客户列表 - 成功")
        void testSyncCustomersAsync_Success() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            List<CustomerInfo> customers = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                customers.add(createTestCustomerInfo((long) i, "客户" + i, null));
            }
            
            List<float[]> vectors = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                vectors.add(testVector);
            }
            
            when(embeddingService.embedBatch(anyList())).thenReturn(vectors);
            when(customerVectorMapper.batchInsert(anyList())).thenReturn(customers.size());

            // When
            vectorSyncService.syncCustomersAsync(customers);

            // Then
            verify(embeddingService, atLeastOnce()).embedBatch(anyList());
        }

        @Test
        @DisplayName("异步同步客户列表 - 功能未启用")
        void testSyncCustomersAsync_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);
            List<CustomerInfo> customers = Arrays.asList(createTestCustomerInfo(1L, "客户", null));

            // When
            vectorSyncService.syncCustomersAsync(customers);

            // Then
            verify(embeddingService, never()).embedBatch(anyList());
        }

        @Test
        @DisplayName("异步同步商品列表 - 成功")
        void testSyncProductsAsync_Success() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            List<Product> products = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                products.add(createTestProduct((long) i, "商品" + i, "CODE" + i));
            }
            
            List<float[]> vectors = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                vectors.add(testVector);
            }
            
            when(embeddingService.embedBatch(anyList())).thenReturn(vectors);
            when(productVectorMapper.batchInsert(anyList())).thenReturn(products.size());

            // When
            vectorSyncService.syncProductsAsync(products);

            // Then
            verify(embeddingService, atLeastOnce()).embedBatch(anyList());
        }
    }

    // ==================== 删除向量测试 ====================

    @Nested
    @DisplayName("删除向量测试")
    class DeleteVectorTests {

        @Test
        @DisplayName("删除客户向量 - 成功")
        void testDeleteCustomerVector_Success() {
            // Given
            Long customerId = 1L;
            when(customerVectorMapper.deleteById(customerId)).thenReturn(1);

            // When
            vectorSyncService.deleteCustomerVector(customerId);

            // Then
            verify(customerVectorMapper).deleteById(customerId);
        }

        @Test
        @DisplayName("删除客户向量 - null ID")
        void testDeleteCustomerVector_NullId() {
            // When
            vectorSyncService.deleteCustomerVector(null);

            // Then
            verify(customerVectorMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("删除商品向量 - 成功")
        void testDeleteProductVector_Success() {
            // Given
            Long productId = 1L;
            when(productVectorMapper.deleteById(productId)).thenReturn(1);

            // When
            vectorSyncService.deleteProductVector(productId);

            // Then
            verify(productVectorMapper).deleteById(productId);
        }

        @Test
        @DisplayName("删除商品向量 - null ID")
        void testDeleteProductVector_NullId() {
            // When
            vectorSyncService.deleteProductVector(null);

            // Then
            verify(productVectorMapper, never()).deleteById(any());
        }
    }

    // ==================== 全量重建测试 ====================

    @Nested
    @DisplayName("全量重建测试")
    class RebuildTests {

        @Test
        @DisplayName("全量重建客户向量")
        void testRebuildAllCustomers() {
            // When
            vectorSyncService.rebuildAllCustomers();

            // Then - 目前是简化实现，仅验证不抛异常
            assertDoesNotThrow(() -> vectorSyncService.rebuildAllCustomers());
        }

        @Test
        @DisplayName("全量重建商品向量")
        void testRebuildAllProducts() {
            // When
            vectorSyncService.rebuildAllProducts();

            // Then - 目前是简化实现，仅验证不抛异常
            assertDoesNotThrow(() -> vectorSyncService.rebuildAllProducts());
        }
    }

    // ==================== 数据验证测试 ====================

    @Nested
    @DisplayName("数据验证测试")
    class DataValidationTests {

        @Test
        @DisplayName("验证客户向量数据完整性")
        void testCustomerVectorDataIntegrity() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            CustomerInfo customer = createTestCustomerInfo(1L, "测试客户", "别名");
            when(embeddingService.embed(any())).thenReturn(testVector);
            
            ArgumentCaptor<CustomerVector> captor = ArgumentCaptor.forClass(CustomerVector.class);
            when(customerVectorMapper.upsert(captor.capture())).thenReturn(1);

            // When
            vectorSyncService.syncCustomer(customer);

            // Then
            CustomerVector captured = captor.getValue();
            assertEquals(1L, captured.getId());
            assertEquals("测试客户", captured.getUnitName());
            assertEquals("别名", captured.getUnitAlias());
            assertNotNull(captured.getVector());
            assertEquals(768, captured.getVector().length);
        }

        @Test
        @DisplayName("验证商品向量数据完整性")
        void testProductVectorDataIntegrity() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(true);
            
            Product product = createTestProduct(1L, "测试商品", "PROD001");
            when(embeddingService.embed(any())).thenReturn(testVector);
            
            ArgumentCaptor<ProductVector> captor = ArgumentCaptor.forClass(ProductVector.class);
            when(productVectorMapper.upsert(captor.capture())).thenReturn(1);

            // When
            vectorSyncService.syncProduct(product);

            // Then
            ProductVector captured = captor.getValue();
            assertEquals(1L, captured.getId());
            assertEquals("测试商品", captured.getProductName());
            assertEquals("PROD001", captured.getProductCode());
            assertNotNull(captured.getVector());
            assertEquals(768, captured.getVector().length);
        }
    }

    // ==================== 辅助方法 ====================

    private CustomerInfo createTestCustomerInfo(Long id, String name, String alias) {
        CustomerInfo ci = new CustomerInfo();
        ci.setId(id);
        ci.setUnitName(name);
        ci.setUnitAlias(alias);
        return ci;
    }

    private Product createTestProduct(Long id, String name, String code) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setProductCode(code);
        return p;
    }
}
