package com.bjdx.rice.business.service.vector;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.CustomerVector;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.bjdx.rice.business.mapper.CustomerVectorMapper;
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

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 余弦相似度计算测试
 * 通过反射测试 VectorSearchServiceImpl 中的 calculateCosineSimilarity 方法
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CosineSimilarityTest {

    @Mock
    private VectorSearchProperties vectorProperties;

    @Mock
    private VectorSearchProperties.SearchProperties searchProperties;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private CustomerVectorMapper customerVectorMapper;

    @Mock
    private CustomerInfoMapper customerInfoMapper;

    @InjectMocks
    private VectorSearchServiceImpl vectorSearchService;

    private Method calculateCosineSimilarityMethod;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射获取私有方法
        calculateCosineSimilarityMethod = VectorSearchServiceImpl.class.getDeclaredMethod(
                "calculateCosineSimilarity", float[].class, float[].class);
        calculateCosineSimilarityMethod.setAccessible(true);
    }

    // ==================== 基本相似度计算测试 ====================

    @Nested
    @DisplayName("基本相似度计算测试")
    class BasicSimilarityTests {

        @Test
        @DisplayName("相同向量 - 相似度应为1.0")
        void testIdenticalVectors() throws Exception {
            // Given
            float[] vector = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vector, vector);

            // Then
            assertEquals(1.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("正交向量 - 相似度应为0")
        void testOrthogonalVectors() throws Exception {
            // Given - 正交向量（点积为0）
            float[] vec1 = {1.0f, 0.0f, 0.0f};
            float[] vec2 = {0.0f, 1.0f, 0.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("相反向量 - 相似度应为-1")
        void testOppositeVectors() throws Exception {
            // Given
            float[] vec1 = {1.0f, 2.0f, 3.0f};
            float[] vec2 = {-1.0f, -2.0f, -3.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(-1.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("部分相似向量 - 验证计算公式")
        void testPartiallySimilarVectors() throws Exception {
            // Given
            float[] vec1 = {1.0f, 1.0f, 0.0f};
            float[] vec2 = {1.0f, 0.0f, 1.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            // cos(θ) = (1*1 + 1*0 + 0*1) / (√2 * √2) = 1/2 = 0.5
            assertEquals(0.5f, similarity, 0.0001f);
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("null向量1 - 返回0")
        void testNullVector1() throws Exception {
            // Given
            float[] vec1 = null;
            float[] vec2 = {1.0f, 2.0f, 3.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("null向量2 - 返回0")
        void testNullVector2() throws Exception {
            // Given
            float[] vec1 = {1.0f, 2.0f, 3.0f};
            float[] vec2 = null;

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("两个null向量 - 返回0")
        void testBothNullVectors() throws Exception {
            // Given
            float[] vec1 = null;
            float[] vec2 = null;

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("不同维度向量 - 返回0")
        void testDifferentDimensionVectors() throws Exception {
            // Given
            float[] vec1 = {1.0f, 2.0f, 3.0f};
            float[] vec2 = {1.0f, 2.0f, 3.0f, 4.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("零向量 - 返回0")
        void testZeroVector() throws Exception {
            // Given
            float[] vec1 = {0.0f, 0.0f, 0.0f};
            float[] vec2 = {1.0f, 2.0f, 3.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("两个零向量 - 返回0")
        void testBothZeroVectors() throws Exception {
            // Given
            float[] vec1 = {0.0f, 0.0f, 0.0f};
            float[] vec2 = {0.0f, 0.0f, 0.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("空数组 - 返回0")
        void testEmptyVectors() throws Exception {
            // Given
            float[] vec1 = {};
            float[] vec2 = {};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f);
        }
    }

    // ==================== 数值精度测试 ====================

    @Nested
    @DisplayName("数值精度测试")
    class PrecisionTests {

        @Test
        @DisplayName("高维向量计算精度")
        void testHighDimensionalVectors() throws Exception {
            // Given - 创建768维向量（实际模型维度）
            float[] vec1 = new float[768];
            float[] vec2 = new float[768];
            for (int i = 0; i < 768; i++) {
                vec1[i] = (float) (Math.random() - 0.5);
                vec2[i] = vec1[i] + (float) (Math.random() * 0.1 - 0.05); // 添加少量噪声
            }

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then - 应该有较高相似度
            assertTrue(similarity > 0.9f);
            assertTrue(similarity <= 1.0f);
        }

        @Test
        @DisplayName("负值向量处理")
        void testNegativeValues() throws Exception {
            // Given
            float[] vec1 = {-1.0f, -2.0f, -3.0f};
            float[] vec2 = {-2.0f, -4.0f, -6.0f}; // vec1 * 2

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then - 相同方向的向量，相似度为1
            assertEquals(1.0f, similarity, 0.0001f);
        }

        @Test
        @DisplayName("极小值向量处理")
        void testVerySmallValues() throws Exception {
            // Given
            float[] vec1 = {1e-10f, 1e-10f, 1e-10f};
            float[] vec2 = {1e-10f, 1e-10f, 1e-10f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(1.0f, similarity, 0.001f);
        }

        @Test
        @DisplayName("极大值向量处理")
        void testVeryLargeValues() throws Exception {
            // Given
            float[] vec1 = {1e6f, 1e6f, 1e6f};
            float[] vec2 = {1e6f, 1e6f, 1e6f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(1.0f, similarity, 0.0001f);
        }
    }

    // ==================== 相似度阈值应用测试 ====================

    @Nested
    @DisplayName("相似度阈值应用测试")
    class ThresholdApplicationTests {

        @Test
        @DisplayName("高相似度向量 - 应超过默认阈值0.75")
        void testHighSimilarityVector() throws Exception {
            // Given - 创建高相似度向量
            float[] vec1 = {1.0f, 0.5f, 0.3f, 0.8f, 0.2f};
            float[] vec2 = {0.95f, 0.48f, 0.29f, 0.79f, 0.21f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertTrue(similarity > 0.75f, "相似度应超过默认阈值0.75");
        }

        @Test
        @DisplayName("低相似度向量 - 应低于默认阈值0.75")
        void testLowSimilarityVector() throws Exception {
            // Given - 创建低相似度向量
            float[] vec1 = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
            float[] vec2 = {0.0f, 1.0f, 0.0f, 0.0f, 0.0f};

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertEquals(0.0f, similarity, 0.0001f, "正交向量相似度应为0");
        }

        @Test
        @DisplayName("边界相似度向量 - 接近阈值")
        void testBoundarySimilarityVector() throws Exception {
            // Given - 计算出一个接近阈值0.75的向量对
            float[] vec1 = {1.0f, 1.0f, 1.0f, 0.0f};
            float[] vec2 = {1.0f, 1.0f, 0.0f, 1.0f};
            // 点积 = 2, |vec1| = √3, |vec2| = √3, 相似度 = 2/3 ≈ 0.667

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertTrue(similarity > 0.6f && similarity < 0.7f);
        }
    }

    // ==================== 实际应用场景测试 ====================

    @Nested
    @DisplayName("实际应用场景测试")
    class RealWorldScenarioTests {

        @Test
        @DisplayName("文本语义相似度模拟")
        void testTextSemanticSimilarity() throws Exception {
            // Given - 模拟两个语义相近的文本向量
            // 假设"北京公司"和"北京企业"的向量表示
            float[] beijingCompany = createNormalizedVector(0.8f, 0.5f, 0.3f, 0.1f);
            float[] beijingEnterprise = createNormalizedVector(0.75f, 0.55f, 0.35f, 0.05f);

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, beijingCompany, beijingEnterprise);

            // Then - 语义相近的文本应有较高相似度
            assertTrue(similarity > 0.9f, "语义相近的文本应有较高相似度");
        }

        @Test
        @DisplayName("文本语义不相似模拟")
        void testTextSemanticDissimilarity() throws Exception {
            // Given - 模拟两个语义不同的文本向量
            // 假设"北京公司"和"上海水果"的向量表示
            float[] beijingCompany = createNormalizedVector(0.8f, 0.5f, 0.3f, 0.1f);
            float[] shanghaiFruit = createNormalizedVector(-0.2f, 0.1f, 0.8f, 0.6f);

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, beijingCompany, shanghaiFruit);

            // Then - 语义不同的文本应有较低相似度
            assertTrue(similarity < 0.5f, "语义不同的文本应有较低相似度");
        }

        @Test
        @DisplayName("高维稀疏向量处理")
        void testSparseHighDimensionalVectors() throws Exception {
            // Given - 创建稀疏的高维向量
            float[] vec1 = new float[768];
            float[] vec2 = new float[768];
            // 只设置少量非零值
            vec1[0] = 1.0f;
            vec1[100] = 0.5f;
            vec2[0] = 0.9f;
            vec2[100] = 0.4f;

            // When
            float similarity = (float) calculateCosineSimilarityMethod.invoke(
                    vectorSearchService, vec1, vec2);

            // Then
            assertTrue(similarity > 0.9f);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建归一化的向量
     */
    private float[] createNormalizedVector(float... values) {
        float norm = 0.0f;
        for (float v : values) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        float[] result = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i] / norm;
        }
        return result;
    }
}
