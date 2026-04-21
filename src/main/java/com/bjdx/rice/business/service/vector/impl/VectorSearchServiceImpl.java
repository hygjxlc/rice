package com.bjdx.rice.business.service.vector.impl;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.mapper.CustomerInfoMapper;
import com.bjdx.rice.business.mapper.ProductMapper;
import com.bjdx.rice.business.service.vector.EmbeddingService;
import com.bjdx.rice.business.service.vector.VectorSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 向量检索服务实现
 * 商品搜索：使用最长公共子序列(LCS)比率算法遍历匹配，区分度远高于N-Gram哈希余弦
 * 客户搜索：使用 PostgreSQL pgvector 进行向量相似度搜索
 * 
 * @author Rice System
 */
@Service
public class VectorSearchServiceImpl implements VectorSearchService {

    private static final Logger logger = LoggerFactory.getLogger(VectorSearchServiceImpl.class);

    @Autowired
    private VectorSearchProperties vectorProperties;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    @Qualifier("pgVectorJdbcTemplate")
    private JdbcTemplate pgVectorJdbcTemplate;

    @Autowired
    private CustomerInfoMapper customerInfoMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public VectorSearchResult searchCustomer(String customerName, int topK, float threshold) {
        logger.info("LCS searchCustomer called, name='{}', topK={}, threshold={}", 
                customerName, topK, String.format("%.2f", threshold));
        
        if (!vectorProperties.getSearch().isEnabled()) {
            logger.warn("LCS searchCustomer: service is not enabled");
            return null;
        }

        try {
            long startTime = System.currentTimeMillis();

            String normalizedQuery = normalizeText(customerName);
            if (normalizedQuery.isEmpty()) {
                logger.warn("LCS searchCustomer: normalized query is empty for '{}'", customerName);
                return null;
            }

            // 加载所有客户
            List<CustomerInfo> allCustomers = customerInfoMapper.selectAll();
            if (allCustomers == null || allCustomers.isEmpty()) {
                logger.warn("LCS searchCustomer: no customers found in database");
                return null;
            }

            // 计算每个客户的 LCS 比率
            List<CustomerMatch> matches = new ArrayList<>();
            for (CustomerInfo customer : allCustomers) {
                String normalizedUnitName = normalizeText(customer.getUnitName());
                if (normalizedUnitName.isEmpty()) {
                    continue;
                }
                float lcsRatio = computeLCSRatio(normalizedQuery, normalizedUnitName);
                if (lcsRatio >= threshold) {
                    matches.add(new CustomerMatch(customer, lcsRatio));
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            if (matches.isEmpty()) {
                logger.debug("LCS search for customer '{}' returned no results (threshold: {}, scanned: {}, time: {}ms)",
                        customerName, String.format("%.2f", threshold), allCustomers.size(), elapsedTime);
                return null;
            }

            // 按 LCS 比率降序排序，取 topK
            matches.sort(Comparator.comparingDouble(CustomerMatch::getScore).reversed());
            if (matches.size() > topK) {
                matches = matches.subList(0, topK);
            }

            // 取最佳匹配
            CustomerMatch bestMatch = matches.get(0);
            CustomerInfo customerInfo = bestMatch.getCustomer();
            float bestSimilarity = bestMatch.getScore();

            // 构建结果
            VectorSearchResult result = new VectorSearchResult(customerInfo.getId(), customerInfo.getUnitName(), bestSimilarity);
            result.setEntity(customerInfo);

            if (vectorProperties.getSearch().isLogMatches()) {
                logger.info("LCS match success: '{}' -> '{}' (score: {}, scanned: {}, time: {}ms)",
                        customerName, customerInfo.getUnitName(), String.format("%.4f", bestSimilarity),
                        allCustomers.size(), elapsedTime);
            }

            return result;

        } catch (Exception e) {
            logger.error("LCS search failed for customer: {}", customerName, e);
            return null;
        }
    }

    @Override
    public VectorSearchResult searchProduct(String productName, int topK, float threshold) {
        logger.info("VectorSearchService: searchProduct called, name='{}', topK={}, threshold={}", 
                productName, topK, String.format("%.2f", threshold));
        
        if (!vectorProperties.getSearch().isEnabled()) {
            logger.warn("LCS searchProduct: service is not enabled");
            return null;
        }

        try {
            long startTime = System.currentTimeMillis();

            // 使用 LCS 比率算法遍历所有商品匹配
            String normalizedQuery = normalizeText(productName);
            if (normalizedQuery.isEmpty()) {
                logger.warn("VectorSearchService: normalized query is empty for '{}'", productName);
                return null;
            }

            // 加载所有商品
            List<Product> allProducts = productMapper.selectAll();
            if (allProducts == null || allProducts.isEmpty()) {
                logger.warn("VectorSearchService: no products found in database");
                return null;
            }

            // 计算每个商品的 LCS 比率
            List<ProductMatch> matches = new ArrayList<>();
            for (Product product : allProducts) {
                String normalizedProductName = normalizeText(product.getProductName());
                if (normalizedProductName.isEmpty()) {
                    continue;
                }
                float lcsRatio = computeLCSRatio(normalizedQuery, normalizedProductName);
                if (lcsRatio >= threshold) {
                    matches.add(new ProductMatch(product, lcsRatio));
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            if (matches.isEmpty()) {
                logger.debug("LCS search for product '{}' returned no results (threshold: {}, scanned: {}, time: {}ms)",
                        productName, String.format("%.2f", threshold), allProducts.size(), elapsedTime);
                return null;
            }

            // 按 LCS 比率降序排序，取 topK
            matches.sort(Comparator.comparingDouble(ProductMatch::getScore).reversed());
            if (matches.size() > topK) {
                matches = matches.subList(0, topK);
            }

            // 取最佳匹配
            ProductMatch bestMatch = matches.get(0);
            Product product = bestMatch.getProduct();
            float bestSimilarity = bestMatch.getScore();

            // 构建结果
            VectorSearchResult result = new VectorSearchResult(product.getId(), product.getProductName(), bestSimilarity);
            result.setEntity(product);

            if (vectorProperties.getSearch().isLogMatches()) {
                logger.info("LCS match success: '{}' -> '{}' (score: {}, scanned: {}, time: {}ms)",
                        productName, product.getProductName(), String.format("%.4f", bestSimilarity), 
                        allProducts.size(), elapsedTime);
            }

            return result;

        } catch (Exception e) {
            logger.error("LCS search failed for product: {}", productName, e);
            return null;
        }
    }

    @Override
    public List<VectorSearchResult> searchProductCandidates(String productName, int topK, float threshold) {
        logger.info("searchProductCandidates called, name='{}', topK={}, threshold={}", 
                productName, topK, String.format("%.2f", threshold));
        
        if (!vectorProperties.getSearch().isEnabled()) {
            logger.warn("searchProductCandidates: service is not enabled");
            return new ArrayList<>();
        }

        try {
            long startTime = System.currentTimeMillis();

            String normalizedQuery = normalizeText(productName);
            if (normalizedQuery.isEmpty()) {
                logger.warn("searchProductCandidates: normalized query is empty for '{}'", productName);
                return new ArrayList<>();
            }

            List<Product> allProducts = productMapper.selectAll();
            if (allProducts == null || allProducts.isEmpty()) {
                logger.warn("searchProductCandidates: no products found in database");
                return new ArrayList<>();
            }

            // 计算每个商品的 LCS 比率
            List<ProductMatch> matches = new ArrayList<>();
            for (Product product : allProducts) {
                String normalizedProductName = normalizeText(product.getProductName());
                if (normalizedProductName.isEmpty()) {
                    continue;
                }
                float lcsRatio = computeLCSRatio(normalizedQuery, normalizedProductName);
                if (lcsRatio >= threshold) {
                    matches.add(new ProductMatch(product, lcsRatio));
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            if (matches.isEmpty()) {
                logger.debug("searchProductCandidates for '{}' returned no results (threshold: {}, scanned: {}, time: {}ms)",
                        productName, String.format("%.2f", threshold), allProducts.size(), elapsedTime);
                return new ArrayList<>();
            }

            // 按 LCS 比率降序排序，取 topK
            matches.sort(Comparator.comparingDouble(ProductMatch::getScore).reversed());
            if (matches.size() > topK) {
                matches = matches.subList(0, topK);
            }

            // 构建结果列表
            List<VectorSearchResult> results = new ArrayList<>();
            for (ProductMatch match : matches) {
                Product product = match.getProduct();
                VectorSearchResult result = new VectorSearchResult(
                        product.getId(), product.getProductName(), match.getScore());
                result.setEntity(product);
                results.add(result);
            }

            if (vectorProperties.getSearch().isLogMatches()) {
                logger.info("searchProductCandidates: '{}' found {} candidates (scanned: {}, time: {}ms)",
                        productName, results.size(), allProducts.size(), elapsedTime);
                for (int i = 0; i < results.size(); i++) {
                    VectorSearchResult r = results.get(i);
                    logger.info("  候选{}: '{}' (score: {})", i + 1, r.getName(), 
                            String.format("%.4f", r.getScore()));
                }
            }

            return results;

        } catch (Exception e) {
            logger.error("searchProductCandidates failed for product: {}", productName, e);
            return new ArrayList<>();
        }
    }

    // ========== 规格提取与比较 ==========

    /**
     * 从商品名称中提取规格数值
     * 提取商品名中第一个 数字+单位 模式的数值部分，用于规格二次区分
     * 例："2.5kg常金东北米" → 2.5, "海天1.9L*6鲜味生抽" → 1.9, "常金东北米" → null
     */
    public static Double extractSpecValue(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return null;
        }
        // 匹配开头的 数字(含小数)+单位 模式
        // 支持: 2.5kg, 1.9L, 500ml, 25Kg, 2200g 等
        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)\\s*(kg|KG|Kg|g|G|L|l|ml|ML|Ml|升|斤)");
        Matcher matcher = pattern.matcher(productName);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从商品名称中提取规格单位
     * 例："2.5kg常金东北米" → "kg", "海天1.9L*6鲜味生抽" → "L", "常金东北米" → null
     */
    public static String extractSpecUnit(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\d+\\.?\\d*\\s*(kg|KG|Kg|g|G|L|l|ml|ML|Ml|升|斤)");
        Matcher matcher = pattern.matcher(productName);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return null;
    }

    /**
     * 规格二次区分：当多个候选LCS分数接近时，通过规格数值选择最佳匹配
     * @param inputName 用户输入的商品名称
     * @param candidates 候选结果列表（已按LCS分数降序排列）
     * @param scoreGapThreshold LCS分数差距阈值，差距大于此值则直接返回top1
     * @return 最佳匹配结果
     */
    public static VectorSearchResult resolveSpecTiebreak(String inputName, 
            List<VectorSearchResult> candidates, float scoreGapThreshold) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        // 只有1个候选，直接返回
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        // top1 领先 top2 超过阈值，直接返回 top1
        float top1Score = candidates.get(0).getScore();
        float top2Score = candidates.get(1).getScore();
        if (top1Score - top2Score > scoreGapThreshold) {
            return candidates.get(0);
        }

        // 分数接近，进行规格二次区分
        Double inputSpecValue = extractSpecValue(inputName);
        String inputSpecUnit = extractSpecUnit(inputName);

        // 用户输入没有规格信息，无法区分，返回LCS最高分
        if (inputSpecValue == null) {
            return candidates.get(0);
        }

        // 在分数接近的候选中，找规格数值最匹配的
        VectorSearchResult bestMatch = candidates.get(0);
        double minSpecDiff = Double.MAX_VALUE;

        for (VectorSearchResult candidate : candidates) {
            // 只在分数接近的候选中比较（与top1差距不超过阈值）
            if (top1Score - candidate.getScore() > scoreGapThreshold) {
                break;
            }
            Double candidateSpecValue = extractSpecValue(candidate.getName());
            if (candidateSpecValue != null) {
                // 规格数值差值
                double specDiff = Math.abs(inputSpecValue - candidateSpecValue);
                // 单位一致时差值权重更高（单位不同需要换算，但简化处理先按数值比较）
                String candidateSpecUnit = extractSpecUnit(candidate.getName());
                if (inputSpecUnit != null && candidateSpecUnit != null 
                        && isSameUnitCategory(inputSpecUnit, candidateSpecUnit)) {
                    // 单位同类（如kg和g，L和ml），需要统一换算
                    specDiff = Math.abs(normalizeToBaseUnit(inputSpecValue, inputSpecUnit) 
                            - normalizeToBaseUnit(candidateSpecValue, candidateSpecUnit));
                }
                if (specDiff < minSpecDiff) {
                    minSpecDiff = specDiff;
                    bestMatch = candidate;
                }
            }
        }

        return bestMatch;
    }

    /**
     * 判断两个单位是否属于同一类别（重量/体积）
     */
    private static boolean isSameUnitCategory(String unit1, String unit2) {
        String[] weightUnits = {"kg", "g", "斤"};
        String[] volumeUnits = {"l", "ml", "升"};
        String u1 = unit1.toLowerCase();
        String u2 = unit2.toLowerCase();
        boolean u1IsWeight = false, u1IsVolume = false, u2IsWeight = false, u2IsVolume = false;
        for (String w : weightUnits) { if (u1.equals(w)) u1IsWeight = true; if (u2.equals(w)) u2IsWeight = true; }
        for (String v : volumeUnits) { if (u1.equals(v)) u1IsVolume = true; if (u2.equals(v)) u2IsVolume = true; }
        return (u1IsWeight && u2IsWeight) || (u1IsVolume && u2IsVolume);
    }

    /**
     * 将规格数值统一换算到基本单位（kg→g, L→ml）
     */
    private static double normalizeToBaseUnit(double value, String unit) {
        String u = unit.toLowerCase();
        if ("kg".equals(u) || "斤".equals(u)) {
            return value * 1000; // kg→g, 斤≈500g
        } else if ("l".equals(u) || "升".equals(u)) {
            return value * 1000; // L→ml
        }
        // g, ml 已经是基本单位
        return value;
    }

    // ========== LCS 比率算法 ==========

    /**
     * 文本归一化：小写 + 去除非中英文数字字符
     */
    private String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String result = text.toLowerCase().trim();
        result = result.replaceAll("[^\\u4e00-\\u9fa5a-z0-9]", "");
        return result;
    }

    /**
     * 计算最长公共子序列(LCS)比率
     * ratio = LCS长度 / min(len1, len2)
     * 相比N-Gram哈希余弦，LCS比率对中文短文本商品名匹配区分度更高
     */
    private float computeLCSRatio(String text1, String text2) {
        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0f;
        }
        int m = text1.length();
        int n = text2.length();
        // 使用滚动数组优化空间
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    curr[j] = prev[j - 1] + 1;
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
            java.util.Arrays.fill(curr, 0);
        }
        int lcsLen = prev[n];
        return (float) lcsLen / Math.min(m, n);
    }

    /**
     * 商品匹配结果内部类
     */
    private static class ProductMatch {
        private final Product product;
        private final float score;

        ProductMatch(Product product, float score) {
            this.product = product;
            this.score = score;
        }

        Product getProduct() { return product; }
        float getScore() { return score; }
    }

    /**
     * 客户匹配结果内部类
     */
    private static class CustomerMatch {
        private final CustomerInfo customer;
        private final float score;

        CustomerMatch(CustomerInfo customer, float score) {
            this.customer = customer;
            this.score = score;
        }

        CustomerInfo getCustomer() { return customer; }
        float getScore() { return score; }
    }

    @Override
    public boolean isAvailable() {
        return vectorProperties.getSearch().isEnabled();
    }

    /**
     * 将 float 数组转换为 PostgreSQL vector 字符串格式
     */
    private String arrayToVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", vector[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 向量搜索结果行
     */
    private static class VectorSearchRow {
        private Long id;
        private String name;
        private float similarity;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public float getSimilarity() { return similarity; }
        public void setSimilarity(float similarity) { this.similarity = similarity; }
    }

    /**
     * 客户向量搜索 RowMapper
     */
    private static class VectorSearchRowMapper implements RowMapper<VectorSearchRow> {
        @Override
        public VectorSearchRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            VectorSearchRow row = new VectorSearchRow();
            row.setId(rs.getLong("id"));
            row.setName(rs.getString("unit_name"));
            row.setSimilarity(rs.getFloat("similarity"));
            return row;
        }
    }

    /**
     * 商品向量搜索 RowMapper
     */
    private static class ProductVectorSearchRowMapper implements RowMapper<VectorSearchRow> {
        @Override
        public VectorSearchRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            VectorSearchRow row = new VectorSearchRow();
            row.setId(rs.getLong("id"));
            row.setName(rs.getString("product_name"));
            row.setSimilarity(rs.getFloat("similarity"));
            return row;
        }
    }
}
