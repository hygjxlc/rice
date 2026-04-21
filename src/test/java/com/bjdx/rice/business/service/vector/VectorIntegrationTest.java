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
import com.bjdx.rice.business.service.vector.impl.VectorSyncServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 向量服务集成测试
 * 测试各服务之间的协作和数据流
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VectorIntegrationTest {

    @Mock
    private VectorSearchProperties vectorProperties;

    @Mock
    private VectorSearchProperties.SearchProperties searchProperties;

    @Mock
    private VectorSearchProperties.EmbeddingProperties embeddingProperties;

    @Mock
    private VectorSearchProperties.LocalModelProperties localModelProperties;

    @Mock
    private VectorSearchProperties.ApiConfigProperties apiConfigProperties;

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

    @InjectMocks
    private VectorSyncServiceImpl vectorSyncService;

    private static final int VECTOR_DIMENSION = 768;

    @BeforeEach
    void setUp() {
        // 设置默认配置
        when(vectorProperties.getSearch()).thenReturn(searchProperties);
        when(vectorProperties.getEmbedding()).thenReturn(embeddingProperties);
        when(embeddingProperties.getLocalModel()).thenReturn(localModelProperties);
        when(embeddingProperties.getApiConfig()).thenReturn(apiConfigProperties);
        when(localModelProperties.getDimension()).thenReturn(VECTOR_DIMENSION);
        when(searchProperties.isEnabled()).thenReturn(true);
        when(searchProperties.getSimilarityThreshold()).thenReturn(0.75f);
        when(searchProperties.getMaxResults()).thenReturn(3);
        when(searchProperties.isLogMatches()).thenReturn(true);
        when(embeddingService.isAvailable()).thenReturn(true);
    }

    // ==================== 服务协作集成测试 ====================

    @Nested
    @DisplayName("服务协作集成测试")
    class ServiceCollaborationTests {

        @Test
        @Order(1)
        @DisplayName("完整客户同步和搜索流程")
        void testCompleteCustomerSyncAndSearchFlow() {
            // Given
            CustomerInfo customer = createTestCustomer(1L, "北京科技有限公司", "北科");
            float[] vector = createTestVector();
            
            when(embeddingService.embed("北京科技有限公司 北科")).thenReturn(vector);
            when(customerVectorMapper.upsert(any(CustomerVector.class))).thenReturn(1);

            // When - 同步客户
            vectorSyncService.syncCustomer(customer);

            // Then - 验证同步过程
            verify(embeddingService).embed("北京科技有限公司 北科");
            verify(customerVectorMapper).upsert(argThat(cv -> 
                    cv.getId().equals(1L) && 
                    cv.getUnitName().equals("北京科技有限公司")
            ));

            // Given - 准备搜索
            CustomerVector storedVector = new CustomerVector(1L, "北京科技有限公司", "北科", vector);
            when(customerVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(storedVector));
            when(embeddingService.embed("北京科技")).thenReturn(vector);
            when(customerInfoMapper.selectByPrimaryKey(1L)).thenReturn(customer);

            // When - 搜索客户
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer("北京科技", 3, 0.75f);

            // Then - 验证搜索结果
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1L, result.getId());
        }

        @Test
        @Order(2)
        @DisplayName("完整商品同步和搜索流程")
        void testCompleteProductSyncAndSearchFlow() {
            // Given
            Product product = createTestProduct(1L, "五常大米", "WCM001");
            float[] vector = createTestVector();
            
            when(embeddingService.embed("五常大米 WCM001")).thenReturn(vector);
            when(productVectorMapper.upsert(any(ProductVector.class))).thenReturn(1);

            // When - 同步商品
            vectorSyncService.syncProduct(product);

            // Then - 验证同步过程
            verify(embeddingService).embed("五常大米 WCM001");
            verify(productVectorMapper).upsert(argThat(pv -> 
                    pv.getId().equals(1L) && 
                    pv.getProductName().equals("五常大米")
            ));
        }

        @Test
        @Order(3)
        @DisplayName("批量同步流程")
        void testBatchSyncFlow() {
            // Given
            List<CustomerInfo> customers = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                customers.add(createTestCustomer((long) i, "客户" + i, null));
            }
            
            List<float[]> vectors = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                vectors.add(createTestVector());
            }
            
            when(embeddingService.embedBatch(anyList())).thenReturn(vectors);
            when(customerVectorMapper.batchInsert(anyList())).thenReturn(10);

            // When
            vectorSyncService.syncCustomersAsync(customers);

            // Then - 验证批量处理被调用
            verify(embeddingService, atLeastOnce()).embedBatch(anyList());
        }
    }

    // ==================== 服务可用性测试 ====================

    @Nested
    @DisplayName("服务可用性测试")
    class ServiceAvailabilityTests {

        @Test
        @Order(10)
        @DisplayName("服务可用性 - 全部条件满足")
        void testServiceAvailability_AllConditionsMet() {
            // When
            boolean available = vectorSearchService.isAvailable();

            // Then
            assertTrue(available);
        }

        @Test
        @Order(11)
        @DisplayName("服务可用性 - 功能未启用")
        void testServiceAvailability_FeatureDisabled() {
            // Given
            when(searchProperties.isEnabled()).thenReturn(false);

            // When
            boolean available = vectorSearchService.isAvailable();

            // Then
            assertFalse(available);
        }

        @Test
        @Order(12)
        @DisplayName("服务可用性 - 嵌入服务不可用")
        void testServiceAvailability_EmbeddingNotAvailable() {
            // Given
            when(embeddingService.isAvailable()).thenReturn(false);

            // When
            boolean available = vectorSearchService.isAvailable();

            // Then
            assertFalse(available);
        }
    }

    // ==================== 错误恢复测试 ====================

    @Nested
    @DisplayName("错误恢复测试")
    class ErrorRecoveryTests {

        @Test
        @Order(20)
        @DisplayName("搜索失败后服务仍可继续工作")
        void testServiceRecoversFromSearchFailure() {
            // Given - 第一次搜索失败
            when(embeddingService.embed("失败测试")).thenThrow(new RuntimeException("搜索失败"));
            
            // When
            VectorSearchService.VectorSearchResult failedResult = vectorSearchService.searchCustomer("失败测试", 3, 0.75f);
            
            // Then
            assertNull(failedResult);

            // Given - 第二次搜索应该正常
            float[] vector = createTestVector();
            when(embeddingService.embed("正常测试")).thenReturn(vector);
            CustomerVector cv = new CustomerVector(1L, "正常客户", null, vector);
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(cv));
            CustomerInfo customer = createTestCustomer(1L, "正常客户", null);
            when(customerInfoMapper.selectByPrimaryKey(1L)).thenReturn(customer);

            // When
            VectorSearchService.VectorSearchResult successResult = vectorSearchService.searchCustomer("正常测试", 3, 0.75f);

            // Then
            assertNotNull(successResult);
        }

        @Test
        @Order(21)
        @DisplayName("同步失败不影响后续同步")
        void testSyncFailureDoesNotAffectSubsequentSyncs() {
            // Given - 第一次同步失败
            CustomerInfo failingCustomer = createTestCustomer(1L, "失败客户", null);
            when(embeddingService.embed("失败客户")).thenThrow(new RuntimeException("同步失败"));

            // When
            assertDoesNotThrow(() -> vectorSyncService.syncCustomer(failingCustomer));

            // Given - 第二次同步应该正常
            CustomerInfo successCustomer = createTestCustomer(2L, "成功客户", null);
            float[] vector = createTestVector();
            when(embeddingService.embed("成功客户")).thenReturn(vector);
            when(customerVectorMapper.upsert(any())).thenReturn(1);

            // When
            vectorSyncService.syncCustomer(successCustomer);

            // Then
            verify(customerVectorMapper).upsert(argThat(cv -> cv.getId().equals(2L)));
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @Order(40)
        @DisplayName("极高相似度阈值")
        void testVeryHighSimilarityThreshold() {
            // Given
            float[] vector = createTestVector();
            when(embeddingService.embed(anyString())).thenReturn(vector);
            // 模拟没有结果满足阈值
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), eq(0.99f)))
                    .thenReturn(Collections.emptyList());

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer("测试", 3, 0.99f);

            // Then
            assertNull(result);
        }

        @Test
        @Order(41)
        @DisplayName("极低相似度阈值")
        void testVeryLowSimilarityThreshold() {
            // Given
            float[] vector = createTestVector();
            when(embeddingService.embed(anyString())).thenReturn(vector);
            CustomerVector cv = new CustomerVector(1L, "测试客户", null, vector);
            when(customerVectorMapper.searchByVectorWithThreshold(any(), anyInt(), eq(0.01f)))
                    .thenReturn(Collections.singletonList(cv));
            CustomerInfo customer = createTestCustomer(1L, "测试客户", null);
            when(customerInfoMapper.selectByPrimaryKey(1L)).thenReturn(customer);

            // When
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer("测试", 3, 0.01f);

            // Then
            assertNotNull(result);
        }

        @Test
        @Order(42)
        @DisplayName("大量批量同步")
        void testLargeBatchSync() {
            // Given
            List<CustomerInfo> customers = new ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                customers.add(createTestCustomer((long) i, "客户" + i, null));
            }
            
            List<float[]> vectors = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                vectors.add(createTestVector());
            }
            
            when(embeddingService.embedBatch(anyList())).thenReturn(vectors);
            when(customerVectorMapper.batchInsert(anyList())).thenReturn(100);

            // When
            vectorSyncService.syncCustomersAsync(customers);

            // Then - 验证批量处理
            verify(embeddingService, atLeastOnce()).embedBatch(anyList());
        }
    }

    // ==================== 数据完整性测试 ====================

    @Nested
    @DisplayName("数据完整性测试")
    class DataIntegrityTests {

        @Test
        @Order(50)
        @DisplayName("客户向量数据完整性")
        void testCustomerVectorDataIntegrity() {
            // Given
            CustomerInfo customer = createTestCustomer(1L, "测试公司", "测试别名");
            float[] vector = createTestVector();
            
            when(embeddingService.embed(any())).thenReturn(vector);
            
            // When
            vectorSyncService.syncCustomer(customer);
            
            // Then - 验证传递给 mapper 的数据完整性
            verify(customerVectorMapper).upsert(argThat(cv -> 
                    cv.getId().equals(1L) &&
                    cv.getUnitName().equals("测试公司") &&
                    cv.getUnitAlias().equals("测试别名") &&
                    cv.getVector() == vector
            ));
        }

        @Test
        @Order(51)
        @DisplayName("商品向量数据完整性")
        void testProductVectorDataIntegrity() {
            // Given
            Product product = createTestProduct(1L, "测试商品", "TEST001");
            float[] vector = createTestVector();
            
            when(embeddingService.embed(any())).thenReturn(vector);
            
            // When
            vectorSyncService.syncProduct(product);
            
            // Then - 验证传递给 mapper 的数据完整性
            verify(productVectorMapper).upsert(argThat(pv -> 
                    pv.getId().equals(1L) &&
                    pv.getProductName().equals("测试商品") &&
                    pv.getProductCode().equals("TEST001") &&
                    pv.getVector() == vector
            ));
        }
    }

    // ==================== 删除向量测试 ====================

    @Nested
    @DisplayName("删除向量测试")
    class DeleteVectorTests {

        @Test
        @Order(60)
        @DisplayName("删除客户向量")
        void testDeleteCustomerVector() {
            // Given
            Long customerId = 1L;
            when(customerVectorMapper.deleteById(customerId)).thenReturn(1);

            // When
            vectorSyncService.deleteCustomerVector(customerId);

            // Then
            verify(customerVectorMapper).deleteById(customerId);
        }

        @Test
        @Order(61)
        @DisplayName("删除商品向量")
        void testDeleteProductVector() {
            // Given
            Long productId = 1L;
            when(productVectorMapper.deleteById(productId)).thenReturn(1);

            // When
            vectorSyncService.deleteProductVector(productId);

            // Then
            verify(productVectorMapper).deleteById(productId);
        }

        @Test
        @Order(62)
        @DisplayName("删除null ID向量")
        void testDeleteNullIdVector() {
            // When
            vectorSyncService.deleteCustomerVector(null);
            vectorSyncService.deleteProductVector(null);

            // Then
            verify(customerVectorMapper, never()).deleteById(any());
            verify(productVectorMapper, never()).deleteById(any());
        }
    }

    // ==================== 辅助方法 ====================

    private CustomerInfo createTestCustomer(Long id, String name, String alias) {
        CustomerInfo customer = new CustomerInfo();
        customer.setId(id);
        customer.setUnitName(name);
        customer.setUnitAlias(alias);
        return customer;
    }

    private Product createTestProduct(Long id, String name, String code) {
        Product product = new Product();
        product.setId(id);
        product.setProductName(name);
        product.setProductCode(code);
        return product;
    }

    private float[] createTestVector() {
        float[] vector = new float[VECTOR_DIMENSION];
        for (int i = 0; i < VECTOR_DIMENSION; i++) {
            vector[i] = (float) (Math.random() * 2 - 1);
        }
        return vector;
    }
}
