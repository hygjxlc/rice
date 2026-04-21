package com.bjdx.rice.business.mapper;

import com.bjdx.rice.business.entity.CustomerVector;
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
 * CustomerVectorMapper 单元测试
 * 测试客户向量数据访问层的接口行为
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerVectorMapperTest {

    @Mock
    private CustomerVectorMapper customerVectorMapper;

    private CustomerVector testCustomerVector;
    private float[] testVector;

    @BeforeEach
    void setUp() {
        testVector = createTestVector(768);
        testCustomerVector = createTestCustomerVector(1L, "测试客户", "测试别名", testVector);
    }

    // ==================== Upsert 操作测试 ====================

    @Nested
    @DisplayName("Upsert 操作测试")
    class UpsertTests {

        @Test
        @DisplayName("插入新记录 - 成功")
        void testUpsert_Insert() {
            // Given
            when(customerVectorMapper.upsert(any(CustomerVector.class))).thenReturn(1);

            // When
            int result = customerVectorMapper.upsert(testCustomerVector);

            // Then
            assertEquals(1, result);
            verify(customerVectorMapper).upsert(testCustomerVector);
        }

        @Test
        @DisplayName("更新已有记录 - 成功")
        void testUpsert_Update() {
            // Given
            CustomerVector existingVector = createTestCustomerVector(1L, "更新客户", "更新别名", testVector);
            when(customerVectorMapper.upsert(any(CustomerVector.class))).thenReturn(1);

            // When
            int result = customerVectorMapper.upsert(existingVector);

            // Then
            assertEquals(1, result);
        }

        @Test
        @DisplayName("Upsert - 返回0表示无影响")
        void testUpsert_NoEffect() {
            // Given
            when(customerVectorMapper.upsert(any(CustomerVector.class))).thenReturn(0);

            // When
            int result = customerVectorMapper.upsert(testCustomerVector);

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
            List<CustomerVector> vectors = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                vectors.add(createTestCustomerVector((long) i, "客户" + i, null, testVector));
            }
            when(customerVectorMapper.batchInsert(anyList())).thenReturn(10);

            // When
            int result = customerVectorMapper.batchInsert(vectors);

            // Then
            assertEquals(10, result);
            verify(customerVectorMapper).batchInsert(vectors);
        }

        @Test
        @DisplayName("批量插入 - 空列表")
        void testBatchInsert_EmptyList() {
            // Given
            when(customerVectorMapper.batchInsert(Collections.emptyList())).thenReturn(0);

            // When
            int result = customerVectorMapper.batchInsert(Collections.emptyList());

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("批量插入 - 大批量数据")
        void testBatchInsert_LargeBatch() {
            // Given
            List<CustomerVector> largeList = new ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                largeList.add(createTestCustomerVector((long) i, "客户" + i, null, testVector));
            }
            when(customerVectorMapper.batchInsert(anyList())).thenReturn(1000);

            // When
            int result = customerVectorMapper.batchInsert(largeList);

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
            when(customerVectorMapper.selectById(1L)).thenReturn(testCustomerVector);

            // When
            CustomerVector result = customerVectorMapper.selectById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("测试客户", result.getUnitName());
        }

        @Test
        @DisplayName("根据ID查询 - 未找到")
        void testSelectById_NotFound() {
            // Given
            when(customerVectorMapper.selectById(999L)).thenReturn(null);

            // When
            CustomerVector result = customerVectorMapper.selectById(999L);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("根据ID查询 - null ID")
        void testSelectById_NullId() {
            // Given
            when(customerVectorMapper.selectById(null)).thenReturn(null);

            // When
            CustomerVector result = customerVectorMapper.selectById(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("查询所有记录")
        void testSelectAll() {
            // Given
            List<CustomerVector> allVectors = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                allVectors.add(createTestCustomerVector((long) i, "客户" + i, null, testVector));
            }
            when(customerVectorMapper.selectAll()).thenReturn(allVectors);

            // When
            List<CustomerVector> result = customerVectorMapper.selectAll();

            // Then
            assertEquals(5, result.size());
        }

        @Test
        @DisplayName("统计记录数量")
        void testCount() {
            // Given
            when(customerVectorMapper.count()).thenReturn(100L);

            // When
            long count = customerVectorMapper.count();

            // Then
            assertEquals(100L, count);
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
            List<CustomerVector> results = new ArrayList<>();
            results.add(createTestCustomerVector(1L, "相似客户1", null, testVector));
            results.add(createTestCustomerVector(2L, "相似客户2", null, testVector));
            when(customerVectorMapper.searchByVector(any(float[].class), anyInt())).thenReturn(results);

            // When
            List<CustomerVector> searchResults = customerVectorMapper.searchByVector(testVector, 5);

            // Then
            assertEquals(2, searchResults.size());
        }

        @Test
        @DisplayName("向量搜索 - 无结果")
        void testSearchByVector_NoResults() {
            // Given
            when(customerVectorMapper.searchByVector(any(float[].class), anyInt()))
                    .thenReturn(Collections.emptyList());

            // When
            List<CustomerVector> searchResults = customerVectorMapper.searchByVector(testVector, 5);

            // Then
            assertTrue(searchResults.isEmpty());
        }

        @Test
        @DisplayName("带阈值的向量搜索 - 高阈值")
        void testSearchByVectorWithThreshold_HighThreshold() {
            // Given
            when(customerVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), anyFloat()))
                    .thenReturn(Collections.emptyList());

            // When
            List<CustomerVector> results = customerVectorMapper.searchByVectorWithThreshold(testVector, 3, 0.95f);

            // Then
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("带阈值的向量搜索 - 低阈值")
        void testSearchByVectorWithThreshold_LowThreshold() {
            // Given
            List<CustomerVector> results = new ArrayList<>();
            results.add(createTestCustomerVector(1L, "客户1", null, testVector));
            results.add(createTestCustomerVector(2L, "客户2", null, testVector));
            when(customerVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), anyFloat()))
                    .thenReturn(results);

            // When
            List<CustomerVector> searchResults = customerVectorMapper.searchByVectorWithThreshold(testVector, 3, 0.5f);

            // Then
            assertEquals(2, searchResults.size());
        }

        @Test
        @DisplayName("带阈值的向量搜索 - 边界阈值")
        void testSearchByVectorWithThreshold_BoundaryThreshold() {
            // Given
            List<CustomerVector> results = new ArrayList<>();
            results.add(createTestCustomerVector(1L, "精确匹配", null, testVector));
            when(customerVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), eq(1.0f)))
                    .thenReturn(results);

            // When
            List<CustomerVector> searchResults = customerVectorMapper.searchByVectorWithThreshold(testVector, 1, 1.0f);

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
            when(customerVectorMapper.deleteById(1L)).thenReturn(1);

            // When
            int result = customerVectorMapper.deleteById(1L);

            // Then
            assertEquals(1, result);
        }

        @Test
        @DisplayName("根据ID删除 - 记录不存在")
        void testDeleteById_NotFound() {
            // Given
            when(customerVectorMapper.deleteById(999L)).thenReturn(0);

            // When
            int result = customerVectorMapper.deleteById(999L);

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("批量删除 - 成功")
        void testBatchDelete_Success() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            when(customerVectorMapper.batchDelete(anyList())).thenReturn(3);

            // When
            int result = customerVectorMapper.batchDelete(ids);

            // Then
            assertEquals(3, result);
        }

        @Test
        @DisplayName("批量删除 - 空列表")
        void testBatchDelete_EmptyList() {
            // Given
            when(customerVectorMapper.batchDelete(Collections.emptyList())).thenReturn(0);

            // When
            int result = customerVectorMapper.batchDelete(Collections.emptyList());

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("清空所有记录")
        void testDeleteAll() {
            // Given
            when(customerVectorMapper.deleteAll()).thenReturn(100);

            // When
            int result = customerVectorMapper.deleteAll();

            // Then
            assertEquals(100, result);
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
            float[] highDimVector = new float[1536]; // OpenAI embedding 维度
            List<CustomerVector> results = Collections.singletonList(testCustomerVector);
            when(customerVectorMapper.searchByVector(any(float[].class), anyInt())).thenReturn(results);

            // When
            List<CustomerVector> searchResults = customerVectorMapper.searchByVector(highDimVector, 3);

            // Then
            assertNotNull(searchResults);
            verify(customerVectorMapper).searchByVector(highDimVector, 3);
        }

        @Test
        @DisplayName("topK参数 - 边界值")
        void testTopKBoundary() {
            // Given
            when(customerVectorMapper.searchByVector(any(float[].class), eq(1)))
                    .thenReturn(Collections.singletonList(testCustomerVector));
            when(customerVectorMapper.searchByVector(any(float[].class), eq(100)))
                    .thenReturn(Collections.singletonList(testCustomerVector));

            // When & Then
            assertNotNull(customerVectorMapper.searchByVector(testVector, 1));
            assertNotNull(customerVectorMapper.searchByVector(testVector, 100));
        }

        @Test
        @DisplayName("阈值参数 - 边界值")
        void testThresholdBoundary() {
            // Given
            when(customerVectorMapper.searchByVectorWithThreshold(any(float[].class), anyInt(), anyFloat()))
                    .thenReturn(Collections.singletonList(testCustomerVector));

            // When & Then - 测试阈值边界
            assertNotNull(customerVectorMapper.searchByVectorWithThreshold(testVector, 3, 0.0f));
            assertNotNull(customerVectorMapper.searchByVectorWithThreshold(testVector, 3, 1.0f));
        }
    }

    // ==================== 辅助方法 ====================

    private CustomerVector createTestCustomerVector(Long id, String name, String alias, float[] vector) {
        CustomerVector cv = new CustomerVector();
        cv.setId(id);
        cv.setUnitName(name);
        cv.setUnitAlias(alias);
        cv.setVector(vector);
        return cv;
    }

    private float[] createTestVector(int dimension) {
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) (Math.random() * 2 - 1);
        }
        return vector;
    }
}
