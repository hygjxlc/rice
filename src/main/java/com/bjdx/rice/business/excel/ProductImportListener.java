package com.bjdx.rice.business.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.bjdx.rice.business.entity.Product;
import com.bjdx.rice.business.exception.MyException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;

public class ProductImportListener extends AnalysisEventListener<Map<Integer, String>> {

    private static final Set<String> PRODUCT_NAME_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("商品名称", "产品名称", "食品名称"))
    );
    private static final Set<String> PRODUCT_CODE_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("商品编号", "商品编码"))
    );
    private static final Set<String> PRODUCT_TYPE_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singletonList("商品类型"))
    );
    private static final Set<String> UNIT_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("单位", "计量单位","基本单位"))
    );
    private static final Set<String> PRICE_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("价格", "单价", "售价","最近售价"))
    );
    private static final Set<String> BRAND_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("品牌", "品牌范围", "生产厂家"))
    );
    private static final Set<String> INDICATOR_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("指标说明", "质量指标", "说明", "描述"))
    );
    private static final Set<String> SPEC_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("规格", "产品规格", "商品规格"))
    );
    private static final Set<String> REMARK_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("备注", "备注信息"))
    );

    // 列索引缓存
    private Integer codeIdx;
    private Integer nameIdx;
    private Integer typeIdx;
    private Integer unitIdx;
    private Integer priceIdx;
    private Integer brandIdx;
    private Integer indicatorIdx;
    private Integer specIdx;
    private Integer remarkIdx;

    private final List<Product> products = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if (headMap == null || headMap.isEmpty()) {
            throw new MyException("Excel 表头为空，请检查文件格式");
        }

        Map<String, Integer> headerToIndex = new HashMap<>();
        for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
            String header = entry.getValue();
            if (header != null) {
                headerToIndex.put(header.trim(), entry.getKey());
            }
        }

        codeIdx = findHeaderIndex(headerToIndex, PRODUCT_CODE_HEADERS);
        nameIdx = findHeaderIndex(headerToIndex, PRODUCT_NAME_HEADERS);
        typeIdx = findHeaderIndex(headerToIndex, PRODUCT_TYPE_HEADERS);
        unitIdx = findHeaderIndex(headerToIndex, UNIT_HEADERS);
        priceIdx = findHeaderIndex(headerToIndex, PRICE_HEADERS);
        brandIdx = findHeaderIndex(headerToIndex, BRAND_HEADERS);
        indicatorIdx = findHeaderIndex(headerToIndex, INDICATOR_HEADERS);
        specIdx = findHeaderIndex(headerToIndex, SPEC_HEADERS);
        remarkIdx = findHeaderIndex(headerToIndex, REMARK_HEADERS);

        if (nameIdx == null) {
            throw new MyException("未找到商品名称列，请确保包含以下任一列名：" + PRODUCT_NAME_HEADERS);
        }
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        long rowIndex = context.readRowHolder().getRowIndex() + 1;

        try {
            String productName = getStringValue(data, nameIdx);
            if (StringUtils.isBlank(productName)) {
                return; // 跳过空行
            }

            Product product = new Product();
            product.setProductName(productName);

            if (codeIdx != null) {
                product.setProductCode(getStringValue(data, codeIdx));
            }
            if (typeIdx != null) {
                product.setProductType(getStringValue(data, typeIdx));
            }
            if (unitIdx != null) {
                product.setUnit(getStringValue(data, unitIdx));
            }
            if (specIdx != null) {
                product.setSpecification(getStringValue(data, specIdx));
            }
            if (priceIdx != null) {
                String priceStr = getStringValue(data, priceIdx);
                if (StringUtils.isNotBlank(priceStr)) {
                    try {
                        product.setPrice(new BigDecimal(priceStr));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("价格格式错误，应为数字，当前值: \"" + priceStr + "\"");
                    }
                }
            }
            if (brandIdx != null) {
                product.setBrand(getStringValue(data, brandIdx));
            }
            if (indicatorIdx != null) {
                product.setIndicatorDesc(getStringValue(data, indicatorIdx));
            }
            if (remarkIdx != null) {
                product.setRemarks(getStringValue(data, remarkIdx));
            }

            products.add(product);

        } catch (Exception e) {
            errors.add("第 " + rowIndex + " 行: " + e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 可留空
    }

    private String getStringValue(Map<Integer, String> data, Integer index) {
        if (index == null) return "";
        String value = data.get(index);
        return value == null ? "" : value.trim();
    }

    private Integer findHeaderIndex(Map<String, Integer> headerToIndex, Set<String> aliases) {
        for (String alias : aliases) {
            if (headerToIndex.containsKey(alias)) {
                return headerToIndex.get(alias);
            }
        }
        return null;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<String> getErrors() {
        return errors;
    }
}