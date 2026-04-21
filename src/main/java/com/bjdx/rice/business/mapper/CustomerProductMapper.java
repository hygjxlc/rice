package com.bjdx.rice.business.mapper;

import com.bjdx.rice.business.dto.customerProduct.CustomerProductReqDTO;
import com.bjdx.rice.business.dto.customerProduct.CustomerProductResDTO;
import com.bjdx.rice.business.entity.CustomerProduct;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CustomerProductMapper extends BaseMapper<CustomerProduct>, MySqlMapper<CustomerProduct> {
    BigDecimal getPrice(@Param("customerId") Long customerId, @Param("productId") Long productId);

    BigDecimal getPriceByProductName(@Param("customerId") Long customerId, @Param("productName") String productName);

    // 电子订单价格查询 - 策略1：customer_id + product_name 精确匹配
    CustomerProduct findPriceExactMatch(@Param("customerId") Long customerId, @Param("productId") Long productId, @Param("productName") String productName);

    // 电子订单价格查询 - 策略2：customer_id + product_id 精确 + product_name 核心中文名模糊匹配
    CustomerProduct findPriceFuzzyMatch(@Param("customerId") Long customerId, @Param("productId") Long productId, @Param("coreProductName") String coreProductName);

    CustomerProductResDTO get(Long id);

    List<CustomerProductResDTO> list(@Param("dto") CustomerProductReqDTO dto);

    void insertBatch(@Param("list") List<CustomerProduct> batch);

    // 根据客户ID和产品ID删除数据
    int deleteByCustomerAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);

    List<CustomerProduct> selectByCustomerAndProductIds(@Param("customerIds") List<Long> customerIds, @Param("productIds") List<Long> productIds);

    // 批量更新客户产品信息
    void updateBatch(@Param("list") List<CustomerProduct> list);

    // 根据客户ID和多个产品ID批量删除
    int deleteByCustomerAndProductIds(@Param("customerId") Long customerId,
                                      @Param("productIds") List<Long> productIds);

}
