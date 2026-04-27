package com.bjdx.rice.business.mapper;

import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.business.dto.product.ProductQueryDTO;
import com.bjdx.rice.business.entity.Product;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface ProductMapper extends BaseMapper<Product>, MySqlMapper<Product> {
    Product selectByProductCode(String productCode);

    List<Product> query(@Param("product") ProductQueryDTO product);

    boolean existsByProductCode(String productCode);

    void insertBatch(@Param("list") List<Product> batch);

    // 批量更新商品信息
    void updateBatch(@Param("list") List<Product> batch);

    // 批量查询商品编号
    List<String> selectProductCodesIn(@Param("codes") Collection<String> codes);

    List<DropDownDTO> getAllProducts(String name);

    Product getProductByName(String productName);

    /**
     * 双向模糊匹配：商品名包含关键词 或 关键词包含商品名的核心内容
     * 例：关键词「常金武育粧大米」可匹配到「10kg常金武育粧」
     */
    Product getProductByNameBidirectional(String productName);

    /**
     * 根据商品名称子串查询所有匹配商品（LIKE匹配，返回全部结果）
     * 用于第二层商品匹配策略
     */
    List<Product> getProductsByNameSubstring(String productName);

    /**
     * 根据商品名称精确查询（WHERE product_name = productName）
     * 用于第一层商品匹配策略
     */
    Product getProductByExactName(String productName);

    BigDecimal getPrice(Long productId);

    Long getIdByName(String productName);

    // 根据多个商品名称批量查询ID
    @MapKey("productName")
    List<Map<String, Object>> getIdsByNames(@Param("productNames") List<String> productNames);

}
