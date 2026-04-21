package com.bjdx.rice.business.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductVector 实体类测试
 * 测试商品向量实体的属性和方法
 */
class ProductVectorTest {

    private ProductVector productVector;
    private float[] testVector;

    @BeforeEach
    void setUp() {
        productVector = new ProductVector();
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
            ProductVector pv = new ProductVector();

            // Then
            assertNotNull(pv);
            assertNull(pv.getId());
            assertNull(pv.getProductName());
            assertNull(pv.getProductCode());
            assertNull(pv.getVector());
        }

        @Test
        @DisplayName("全参构造函数")
        void testAllArgsConstructor() {
            // Given
            Long id = 1L;
            String productName = "东北大米";
            String productCode = "RICE001";
            float[] vector = {0.1f, 0.2f, 0.3f};

            // When
            ProductVector pv = new ProductVector(id, productName, productCode, vector);

            // Then
            assertEquals(id, pv.getId());
            assertEquals(productName, pv.getProductName());
            assertEquals(productCode, pv.getProductCode());
            assertArrayEquals(vector, pv.getVector());
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
            productVector.setId(100L);

            // Then
            assertEquals(100L, productVector.getId());
        }

        @Test
        @DisplayName("设置和获取商品名称")
        void testProductNameAccessor() {
            // When
            productVector.setProductName("五常大米");

            // Then
            assertEquals("五常大米", productVector.getProductName());
        }

        @Test
        @DisplayName("设置和获取商品编码")
        void testProductCodeAccessor() {
            // When
            productVector.setProductCode("WCM001");

            // Then
            assertEquals("WCM001", productVector.getProductCode());
        }

        @Test
        @DisplayName("设置和获取向量")
        void testVectorAccessor() {
            // When
            productVector.setVector(testVector);

            // Then
            assertArrayEquals(testVector, productVector.getVector());
        }

        @Test
        @DisplayName("设置和获取元数据")
        void testMetadataAccessor() {
            // When
            productVector.setMetadata("{\"category\":\"大米\"}");

            // Then
            assertEquals("{\"category\":\"大米\"}", productVector.getMetadata());
        }

        @Test
        @DisplayName("设置和获取创建时间")
        void testCreatedAtAccessor() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // When
            productVector.setCreatedAt(now);

            // Then
            assertEquals(now, productVector.getCreatedAt());
        }

        @Test
        @DisplayName("设置和获取更新时间")
        void testUpdatedAtAccessor() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // When
            productVector.setUpdatedAt(now);

            // Then
            assertEquals(now, productVector.getUpdatedAt());
        }
    }

    // ==================== toEmbeddingText 方法测试 ====================

    @Nested
    @DisplayName("toEmbeddingText 方法测试")
    class ToEmbeddingTextTests {

        @Test
        @DisplayName("只有商品名称")
        void testOnlyProductName() {
            // Given
            productVector.setProductName("五常大米");
            productVector.setProductCode(null);

            // When
            String text = productVector.toEmbeddingText();

            // Then
            assertEquals("五常大米", text);
        }

        @Test
        @DisplayName("商品名称和编码")
        void testProductNameAndCode() {
            // Given
            productVector.setProductName("五常大米");
            productVector.setProductCode("WCM001");

            // When
            String text = productVector.toEmbeddingText();

            // Then
            assertEquals("五常大米 WCM001", text);
        }

        @Test
        @DisplayName("商品名称和空编码")
        void testProductNameAndEmptyCode() {
            // Given
            productVector.setProductName("五常大米");
            productVector.setProductCode("");

            // When
            String text = productVector.toEmbeddingText();

            // Then
            assertEquals("五常大米", text);
        }

        @Test
        @DisplayName("null商品名称")
        void testNullProductName() {
            // Given
            productVector.setProductName(null);
            productVector.setProductCode("WCM001");

            // When
            String text = productVector.toEmbeddingText();

            // Then - null 会转换为 "null" 字符串
            assertEquals("null WCM001", text);
        }

        @Test
        @DisplayName("复杂商品编码的处理")
        void testComplexProductCode() {
            // Given
            productVector.setProductName("东北大米");
            productVector.setProductCode("DB-2024-001-A");

            // When
            String text = productVector.toEmbeddingText();

            // Then
            assertEquals("东北大米 DB-2024-001-A", text);
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
            productVector.setId(1L);
            productVector.setProductName("五常大米");
            productVector.setProductCode("WCM001");
            LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 10, 0);
            productVector.setCreatedAt(created);
            productVector.setUpdatedAt(updated);

            // When
            String str = productVector.toString();

            // Then
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("五常大米"));
            assertTrue(str.contains("WCM001"));
            assertTrue(str.contains("ProductVector"));
        }

        @Test
        @DisplayName("空信息的toString")
        void testToStringEmpty() {
            // Given
            ProductVector empty = new ProductVector();

            // When
            String str = empty.toString();

            // Then
            assertTrue(str.contains("ProductVector"));
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
            productVector.setVector(highDimVector);

            // Then
            assertEquals(768, productVector.getVector().length);
        }

        @Test
        @DisplayName("向量修改独立性")
        void testVectorModificationIndependence() {
            // Given
            float[] original = {0.1f, 0.2f, 0.3f};
            productVector.setVector(original);

            // When - 修改原始数组
            original[0] = 0.9f;

            // Then - 获取的向量也应改变（因为是同一引用）
            assertEquals(0.9f, productVector.getVector()[0], 0.001f);
        }

        @Test
        @DisplayName("设置null向量")
        void testNullVector() {
            // When
            productVector.setVector(null);

            // Then
            assertNull(productVector.getVector());
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("超长商品名称")
        void testVeryLongProductName() {
            // Given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("大米");
            }
            String longName = sb.toString();

            // When
            productVector.setProductName(longName);

            // Then
            assertEquals(longName, productVector.getProductName());
        }

        @Test
        @DisplayName("特殊字符商品名称")
        void testSpecialCharacters() {
            // Given
            String specialName = "五常<大米>&\"东北\"\\n\t";

            // When
            productVector.setProductName(specialName);

            // Then
            assertEquals(specialName, productVector.getProductName());
        }

        @Test
        @DisplayName("Unicode字符商品名称")
        void testUnicodeCharacters() {
            // Given
            String unicodeName = "五常大米🍚🌾🏆";

            // When
            productVector.setProductName(unicodeName);

            // Then
            assertEquals(unicodeName, productVector.getProductName());
        }

        @Test
        @DisplayName("ID边界值")
        void testIdBoundary() {
            // Given
            Long maxId = Long.MAX_VALUE;
            Long minId = Long.MIN_VALUE;

            // When & Then
            productVector.setId(maxId);
            assertEquals(maxId, productVector.getId());

            productVector.setId(minId);
            assertEquals(minId, productVector.getId());
        }

        @Test
        @DisplayName("商品编码边界值")
        void testProductCodeBoundary() {
            // Given
            String emptyCode = "";
            String singleCharCode = "A";
            String longCode = "PRODUCT-CODE-2024-VERY-LONG-IDENTIFIER-12345";

            // When & Then
            productVector.setProductCode(emptyCode);
            assertEquals(emptyCode, productVector.getProductCode());

            productVector.setProductCode(singleCharCode);
            assertEquals(singleCharCode, productVector.getProductCode());

            productVector.setProductCode(longCode);
            assertEquals(longCode, productVector.getProductCode());
        }
    }

    // ==================== 实际应用场景测试 ====================

    @Nested
    @DisplayName("实际应用场景测试")
    class RealWorldScenarioTests {

        @Test
        @DisplayName("完整商品向量创建流程")
        void testCompleteProductVectorCreation() {
            // Given
            Long id = 1L;
            String productName = "五常稻花香大米";
            String productCode = "WC-2024-001";
            float[] vector = createTestVector(768);
            LocalDateTime now = LocalDateTime.now();

            // When
            ProductVector pv = new ProductVector(id, productName, productCode, vector);
            pv.setMetadata("{\"brand\":\"五常\",\"weight\":\"5kg\"}");
            pv.setCreatedAt(now);
            pv.setUpdatedAt(now);

            // Then
            assertEquals(id, pv.getId());
            assertEquals(productName, pv.getProductName());
            assertEquals(productCode, pv.getProductCode());
            assertEquals(768, pv.getVector().length);
            assertEquals("五常稻花香大米 WC-2024-001", pv.toEmbeddingText());
            assertNotNull(pv.getMetadata());
            assertEquals(now, pv.getCreatedAt());
            assertEquals(now, pv.getUpdatedAt());
        }

        @Test
        @DisplayName("向量更新场景")
        void testVectorUpdateScenario() {
            // Given - 初始商品向量
            productVector.setId(1L);
            productVector.setProductName("东北大米");
            productVector.setProductCode("DB001");
            productVector.setVector(createTestVector(768));

            // When - 更新向量
            float[] newVector = createTestVector(768);
            productVector.setVector(newVector);
            productVector.setUpdatedAt(LocalDateTime.now());

            // Then
            assertArrayEquals(newVector, productVector.getVector());
            assertNotNull(productVector.getUpdatedAt());
        }
    }

    // ==================== 辅助方法 ====================

    private float[] createTestVector(int dimension) {
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) (Math.random() * 2 - 1); // -1 到 1 之间的随机值
        }
        return vector;
    }
}
