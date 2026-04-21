package com.bjdx.rice.business.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CustomerVector 实体类测试
 * 测试客户向量实体的属性和方法
 */
class CustomerVectorTest {

    private CustomerVector customerVector;
    private float[] testVector;

    @BeforeEach
    void setUp() {
        customerVector = new CustomerVector();
        testVector = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
    }

    // ==================== 构造函数测试 ====================

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造函数")
        void testNoArgsConstructor() {
            // When
            CustomerVector cv = new CustomerVector();

            // Then
            assertNotNull(cv);
            assertNull(cv.getId());
            assertNull(cv.getUnitName());
            assertNull(cv.getUnitAlias());
            assertNull(cv.getVector());
        }

        @Test
        @DisplayName("全参构造函数")
        void testAllArgsConstructor() {
            // Given
            Long id = 1L;
            String unitName = "测试公司";
            String unitAlias = "测试别名";
            float[] vector = {0.1f, 0.2f, 0.3f};

            // When
            CustomerVector cv = new CustomerVector(id, unitName, unitAlias, vector);

            // Then
            assertEquals(id, cv.getId());
            assertEquals(unitName, cv.getUnitName());
            assertEquals(unitAlias, cv.getUnitAlias());
            assertArrayEquals(vector, cv.getVector());
        }
    }

    // ==================== 属性访问器测试 ====================

    @Nested
    @DisplayName("属性访问器测试")
    class AccessorTests {

        @Test
        @DisplayName("设置和获取ID")
        void testIdAccessor() {
            // When
            customerVector.setId(100L);

            // Then
            assertEquals(100L, customerVector.getId());
        }

        @Test
        @DisplayName("设置和获取单位名称")
        void testUnitNameAccessor() {
            // When
            customerVector.setUnitName("北京科技有限公司");

            // Then
            assertEquals("北京科技有限公司", customerVector.getUnitName());
        }

        @Test
        @DisplayName("设置和获取单位别名")
        void testUnitAliasAccessor() {
            // When
            customerVector.setUnitAlias("北科");

            // Then
            assertEquals("北科", customerVector.getUnitAlias());
        }

        @Test
        @DisplayName("设置和获取向量")
        void testVectorAccessor() {
            // When
            customerVector.setVector(testVector);

            // Then
            assertArrayEquals(testVector, customerVector.getVector());
        }

        @Test
        @DisplayName("设置和获取元数据")
        void testMetadataAccessor() {
            // When
            customerVector.setMetadata("{\"source\":\"import\"}");

            // Then
            assertEquals("{\"source\":\"import\"}", customerVector.getMetadata());
        }

        @Test
        @DisplayName("设置和获取创建时间")
        void testCreatedAtAccessor() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // When
            customerVector.setCreatedAt(now);

            // Then
            assertEquals(now, customerVector.getCreatedAt());
        }

        @Test
        @DisplayName("设置和获取更新时间")
        void testUpdatedAtAccessor() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // When
            customerVector.setUpdatedAt(now);

            // Then
            assertEquals(now, customerVector.getUpdatedAt());
        }
    }

    // ==================== toEmbeddingText 方法测试 ====================

    @Nested
    @DisplayName("toEmbeddingText 方法测试")
    class ToEmbeddingTextTests {

        @Test
        @DisplayName("只有单位名称")
        void testOnlyUnitName() {
            // Given
            customerVector.setUnitName("北京科技有限公司");
            customerVector.setUnitAlias(null);

            // When
            String text = customerVector.toEmbeddingText();

            // Then
            assertEquals("北京科技有限公司", text);
        }

        @Test
        @DisplayName("单位名称和别名")
        void testUnitNameAndAlias() {
            // Given
            customerVector.setUnitName("北京科技有限公司");
            customerVector.setUnitAlias("北科");

            // When
            String text = customerVector.toEmbeddingText();

            // Then
            assertEquals("北京科技有限公司 北科", text);
        }

        @Test
        @DisplayName("单位名称和空别名")
        void testUnitNameAndEmptyAlias() {
            // Given
            customerVector.setUnitName("北京科技有限公司");
            customerVector.setUnitAlias("");

            // When
            String text = customerVector.toEmbeddingText();

            // Then
            assertEquals("北京科技有限公司", text);
        }

        @Test
        @DisplayName("null单位名称")
        void testNullUnitName() {
            // Given
            customerVector.setUnitName(null);
            customerVector.setUnitAlias("北科");

            // When
            String text = customerVector.toEmbeddingText();

            // Then - null 会转换为 "null" 字符串
            assertEquals("null 北科", text);
        }

        @Test
        @DisplayName("多个别名的处理")
        void testMultipleAliases() {
            // Given
            customerVector.setUnitName("北京科技有限公司");
            customerVector.setUnitAlias("北科,北京科技,BK");

            // When
            String text = customerVector.toEmbeddingText();

            // Then
            assertEquals("北京科技有限公司 北科,北京科技,BK", text);
        }
    }

    // ==================== toString 方法测试 ====================

    @Nested
    @DisplayName("toString 方法测试")
    class ToStringTests {

        @Test
        @DisplayName("完整信息的toString")
        void testToStringComplete() {
            // Given
            customerVector.setId(1L);
            customerVector.setUnitName("测试公司");
            customerVector.setUnitAlias("测试");
            LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 10, 0);
            customerVector.setCreatedAt(created);
            customerVector.setUpdatedAt(updated);

            // When
            String str = customerVector.toString();

            // Then
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("测试公司"));
            assertTrue(str.contains("测试"));
            assertTrue(str.contains("CustomerVector"));
        }

        @Test
        @DisplayName("空信息的toString")
        void testToStringEmpty() {
            // Given
            CustomerVector empty = new CustomerVector();

            // When
            String str = empty.toString();

            // Then
            assertTrue(str.contains("CustomerVector"));
        }
    }

    // ==================== 向量数据处理测试 ====================

    @Nested
    @DisplayName("向量数据处理测试")
    class VectorDataTests {

        @Test
        @DisplayName("高维向量设置")
        void testHighDimensionalVector() {
            // Given
            float[] highDimVector = new float[768];
            for (int i = 0; i < 768; i++) {
                highDimVector[i] = (float) Math.random();
            }

            // When
            customerVector.setVector(highDimVector);

            // Then
            assertEquals(768, customerVector.getVector().length);
        }

        @Test
        @DisplayName("向量修改独立性")
        void testVectorModificationIndependence() {
            // Given
            float[] original = {0.1f, 0.2f, 0.3f};
            customerVector.setVector(original);

            // When - 修改原始数组
            original[0] = 0.9f;

            // Then - 获取的向量也应改变（因为是同一引用）
            assertEquals(0.9f, customerVector.getVector()[0], 0.001f);
        }

        @Test
        @DisplayName("设置null向量")
        void testNullVector() {
            // When
            customerVector.setVector(null);

            // Then
            assertNull(customerVector.getVector());
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("超长单位名称")
        void testVeryLongUnitName() {
            // Given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("公司");
            }
            String longName = sb.toString();

            // When
            customerVector.setUnitName(longName);

            // Then
            assertEquals(longName, customerVector.getUnitName());
        }

        @Test
        @DisplayName("特殊字符单位名称")
        void testSpecialCharacters() {
            // Given
            String specialName = "北京<科技>&\"公司\"\\n\t";

            // When
            customerVector.setUnitName(specialName);

            // Then
            assertEquals(specialName, customerVector.getUnitName());
        }

        @Test
        @DisplayName("Unicode字符单位名称")
        void testUnicodeCharacters() {
            // Given
            String unicodeName = "北京科技有限公司🎉🏆💯";

            // When
            customerVector.setUnitName(unicodeName);

            // Then
            assertEquals(unicodeName, customerVector.getUnitName());
        }

        @Test
        @DisplayName("ID边界值")
        void testIdBoundary() {
            // Given
            Long maxId = Long.MAX_VALUE;
            Long minId = Long.MIN_VALUE;

            // When & Then
            customerVector.setId(maxId);
            assertEquals(maxId, customerVector.getId());

            customerVector.setId(minId);
            assertEquals(minId, customerVector.getId());
        }
    }
}
