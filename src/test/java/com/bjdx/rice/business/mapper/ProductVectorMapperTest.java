package com.bjdx.rice.business.mapper;

import com.bjdx.rice.business.entity.ProductVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProductVectorMapper 单元测试
 * 测试商品向量数据访问层的接口行为
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductVectorMapperTest {

    @Mock
    private ProductVectorMapper productVectorMapper;

    private ProductVector testProductVector;
    private float[] testVector;

    @BeforeEach
    void setUp() {
        testVector = createTestVector(768);
        testProductVector = createTestProductVector(1L, "测试商品", "PROD001", testVector);
    }

    // ==================== Upsert 操作测试 ====================

    @Nested
    @DisplayName("Upsert 操作测试")
    class UpsertTests {

        @Test
        @DisplayName("插入新记录 - 成功")
        void testUpsert_Insert() {
            // Given
            when(productVectorMapper.upsert(any(ProductVector.class))).thenReturn(1);

            // When
            int result = productVectorMapper.upsert(testProductVector);

            // Then
            assertEquals(1, result);
            verify(productVectorMapper).upsert(testProductVector);
        }

        @Test
        @DisplayName("更新已有记录 - 成功")
        void testUpsert_Update() {
            // Given
            ProductVector existingVector = createTestProductVector(1L, "更新商品", "PROD002", testVector);
            when(productVectorMapper.upsert(any(ProductVector.class))).thenReturn(1);

            // When
            int result = productVectorMapper.upsert(existingVector);

            // Then
            assertEquals(1, result);
        }

        @Test
        @DisplayName("Upsert - 返回0表示无影响")
        void testUpsert_NoEffect() {
            // Given
            when(productVectorMapper.upsert(any(ProductVector.class))).thenReturn(0);

            // When
            int result = productVectorMapper.upsert(testProductVector);

            // Then
            assertEquals(0, result);
        }
    }

    // ==================== 批量插入测试 ====================

    @Nested
    @DisplayName("批量插入测试")
    class BatchInsertTests {

        @Test
        @DisplayName("批量插入 - 成功")
        void testBatchInsert_Success() {
            // Given
            List<ProductVector> vectors = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                vectors.add(createTestProductVector((long) i, "商品" + i, "CODE" + i, testVector));
            }
            when(productVectorMapper.batchInsert(anyList())).thenReturn(10);

            // When
            int result = productVectorMapper.batchInsert(vectors);

            // Then
            assertEquals(10, result);
            verify(productVectorMapper).batchInsert(vectors);
        }

        @Test
        @DisplayName("批量插入 - 空列表")
        void testBatchInsert_EmptyList() {
            // Given
            when(productVectorMapper.batchInsert(Collections.emptyList())).thenReturn(0);

            // When
            int result = productVectorMapper.batchInsert(Collections.emptyList());

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("批量插入 - 大批量数据")
        void testBatchInsert_LargeBatch() {
            // Given
            List<ProductVector> largeList = new ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                largeList.add(createTestProductVector((long) i, "商品" + i, "CODE" + i, testVector));
            }
            when(productVectorMapper.batchInsert(anyList())).thenReturn(1000);

            // When
            int result = productVectorMapper.batchInsert(largeList);

            // Then
            assertEquals(1000, result);
        }
    }

    // ==================== 查询操作测试 ====================

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("根据ID查询 - 找到记录")
        void testSelectById_Found() {
            // Given
            when(productVectorMapper.selectById(1L)).thenReturn(testProductVector);

            // When
            ProductVector result = productVectorMapper.selectById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("测试商品", result.getProductName());
            assertEquals("PROD001", result.getProductCode());
        }

        @Test
        @DisplayName("根据ID查询 - 未找到")
        void testSelectById_NotFound() {
            // Given
            when(productVectorMapper.selectById(999L)).thenReturn(null);

            // When
            ProductVector result = productVectorMapper.selectById(999L);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("根据ID查询 - null ID")
        void testSelectById_NullId() {
            // Given
            when(productVectorMapper.selectById(null)).thenReturn(null);

            // When
            ProductVector result = productVectorMapper.selectById(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("查询所有记录")
        void testSelectAll() {
            // Given
            List<ProductVector> allVectors = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                allVectors.add(createTestProductVector((long) i, "商品" + i, "CODE" + i, testVector));
            }
            when(productVectorMapper.selectAll()).thenReturn(allVectors);

            // When
            List<ProductVector> result = productVectorMapper.selectAll();

            // Then
            assertEquals(5, result.size());
        }

        @Test
        @DisplayName("统计记录数量")
        void testCount() {
            // Given
            when(productVectorMapper.count()).thenReturn(50L);

            // When
            long count = productVectorMapper.count();

            // Then
            assertEquals(50L, count);
        }
    }

    // ==================== 向量搜索测试 ====================

    @Nested
    @DisplayName("向量搜索测试")
    class VectorSearchTests {

        @Test
        @DisplayName("向量搜索 - 有结果")
        void testSearchByVector_WithResults() {
            // Given
            List<ProductVector> results = new ArrayList<>();
            results.add(createTestProductVector(1L, "相似商品1", "P001", testVector));
            results.add(createTestProductVector(2L, "相似商品2", "P002", testVector));
            when(productVectorMapper.searchByVector(any(float[].class), anyInt())).thenReturn(results);

            // When
            List<ProductVector> searchResults = productVectorMapper.searchByVector(testVector, 5);

            // Then
            assertEquals(2, searchResults.size());
        }

        @Test
        @DisplayName("向量搜索 - 无结果")
        void testSearchByVector_NoResults() {
            // Given
            when(productVectorMapper.searchByVector(any(float[].class), anyInt()))
                    .thenReturn(Collections.emptyList());

            // When
            List<ProductVector> searchResults = productVectorMapper.searchByVector(testVector, 5);

            // Then
            assertTrue(searchResults.isEmpty());
        }

        @Test
        @DisplayName("带阈值的向量搜索 - 高阈值")
        void testSearchByVectorWithThreshold_HighThreshold() {
            // Given
            when(productVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), anyFloat()))
                    .thenReturn(Collections.emptyList());

            // When
            List<ProductVector> results = productVectorMapper.searchByVectorWithThreshold(testVector, 3, 0.95f);

            // Then
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("带阈值的向量搜索 - 低阈值")
        void testSearchByVectorWithThreshold_LowThreshold() {
            // Given
            List<ProductVector> results = new ArrayList<>();
            results.add(createTestProductVector(1L, "商品1", "P001", testVector));
            results.add(createTestProductVector(2L, "商品2", "P002", testVector));
            when(productVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), anyFloat()))
                    .thenReturn(results);

            // When
            List<ProductVector> searchResults = productVectorMapper.searchByVectorWithThreshold(testVector, 3, 0.5f);

            // Then
            assertEquals(2, searchResults.size());
        }

        @Test
        @DisplayName("带阈值的向量搜索 - 边界阈值")
        void testSearchByVectorWithThreshold_BoundaryThreshold() {
            // Given
            List<ProductVector> results = new ArrayList<>();
            results.add(createTestProductVector(1L, "精确匹配", "P001", testVector));
            when(productVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), eq(1.0f)))
                    .thenReturn(results);

            // When
            List<ProductVector> searchResults = productVectorMapper.searchByVectorWithThreshold(testVector, 1, 1.0f);

            // Then
            assertEquals(1, searchResults.size());
        }
    }

    // ==================== 删除操作测试 ====================

    @Nested
    @DisplayName("删除操作测试")
    class DeleteTests {

        @Test
        @DisplayName("根据ID删除 - 成功")
        void testDeleteById_Success() {
            // Given
            when(productVectorMapper.deleteById(1L)).thenReturn(1);

            // When
            int result = productVectorMapper.deleteById(1L);

            // Then
            assertEquals(1, result);
        }

        @Test
        @DisplayName("根据ID删除 - 记录不存在")
        void testDeleteById_NotFound() {
            // Given
            when(productVectorMapper.deleteById(999L)).thenReturn(0);

            // When
            int result = productVectorMapper.deleteById(999L);

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("批量删除 - 成功")
        void testBatchDelete_Success() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            when(productVectorMapper.batchDelete(anyList())).thenReturn(3);

            // When
            int result = productVectorMapper.batchDelete(ids);

            // Then
            assertEquals(3, result);
        }

        @Test
        @DisplayName("批量删除 - 空列表")
        void testBatchDelete_EmptyList() {
            // Given
            when(productVectorMapper.batchDelete(Collections.emptyList())).thenReturn(0);

            // When
            int result = productVectorMapper.batchDelete(Collections.emptyList());

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("清空所有记录")
        void testDeleteAll() {
            // Given
            when(productVectorMapper.deleteAll()).thenReturn(50);

            // When
            int result = productVectorMapper.deleteAll();

            // Then
            assertEquals(50, result);
        }
    }

    // ==================== 参数验证测试 ====================

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("向量参数 - 高维向量")
        void testHighDimensionalVector() {
            // Given
            float[] highDimVector = new float[1536];
            List<ProductVector> results = Collections.singletonList(testProductVector);
            when(productVectorMapper.searchByVector(any(float[].class), anyInt())).thenReturn(results);

            // When
            List<ProductVector> searchResults = productVectorMapper.searchByVector(highDimVector, 3);

            // Then
            assertNotNull(searchResults);
            verify(productVectorMapper).searchByVector(highDimVector, 3);
        }

        @Test
        @DisplayName("topK参数 - 边界值")
        void testTopKBoundary() {
            // Given
            when(productVectorMapper.searchByVector(any(float[].class), eq(1)))
                    .thenReturn(Collections.singletonList(testProductVector));
            when(productVectorMapper.searchByVector(any(float[].class), eq(100)))
                    .thenReturn(Collections.singletonList(testProductVector));

            // When & Then
            assertNotNull(productVectorMapper.searchByVector(testVector, 1));
            assertNotNull(productVectorMapper.searchByVector(testVector, 100));
        }

        @Test
        @DisplayName("阈值参数 - 边界值")
        void testThresholdBoundary() {
            // Given
            when(productVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(testProductVector));

            // When & Then
            assertNotNull(productVectorMapper.searchByVectorWithThreshold(testVector, 3, 0.0f));
            assertNotNull(productVectorMapper.searchByVectorWithThreshold(testVector, 3, 1.0f));
        }
    }

    // ==================== 实际场景测试 ====================

    @Nested
    @DisplayName("实际场景测试")
    class RealScenarioTests {

        @Test
        @DisplayName("完整CRUD流程")
        void testCompleteCrudFlow() {
            // Given
            ProductVector newProduct = createTestProductVector(10L, "新产品", "NEW001", testVector);
            
            // Insert
            when(productVectorMapper.upsert(newProduct)).thenReturn(1);
            assertEquals(1, productVectorMapper.upsert(newProduct));

            // Select
            when(productVectorMapper.selectById(10L)).thenReturn(newProduct);
            ProductVector found = productVectorMapper.selectById(10L);
            assertNotNull(found);
            assertEquals("新产品", found.getProductName());

            // Update
            newProduct.setProductName("更新产品");
            when(productVectorMapper.upsert(newProduct)).thenReturn(1);
            assertEquals(1, productVectorMapper.upsert(newProduct));

            // Delete
            when(productVectorMapper.deleteById(10L)).thenReturn(1);
            assertEquals(1, productVectorMapper.deleteById(10L));
        }

        @Test
        @DisplayName("批量导入场景")
        void testBatchImportScenario() {
            // Given
            List<ProductVector> importList = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                importList.add(createTestProductVector((long) i, "商品" + i, "CODE" + i, testVector));
            }
            when(productVectorMapper.batchInsert(anyList())).thenReturn(100);

            // When
            int result = productVectorMapper.batchInsert(importList);

            // Then
            assertEquals(100, result);
        }
    }

    // ==================== 辅助方法 ====================

    private ProductVector createTestProductVector(Long id, String name, String code, float[] vector) {
        ProductVector pv = new ProductVector();
        pv.setId(id);
        pv.setProductName(name);
        pv.setProductCode(code);
        pv.setVector(vector);
        return pv;
    }

    private float[] createTestVector(int dimension) {
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) (Math.random() * 2 - 1);
        }
        return vector;
    }
}
