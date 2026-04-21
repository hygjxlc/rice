package com.bjdx.rice.business.service;

import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.dto.customerProduct.CustomerProductReqDTO;
import com.bjdx.rice.business.dto.customerProduct.CustomerProductResDTO;
import com.bjdx.rice.business.entity.CustomerProduct;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

public interface CustomerProductService {

    BigDecimal getPrice(Long customerId, Long productId);

    BigDecimal getPriceByProductName(Long customerId, String productName);

    // 电子订单价格查询 - 策略1：customer_id + product_name 精确匹配，按结束时间倒序
    CustomerProduct findPriceExactMatch(Long customerId, Long productId, String productName);

    // 电子订单价格查询 - 策略2：customer_id + product_id 精确 + product_name 核心中文名模糊匹配，按结束时间倒序
    CustomerProduct findPriceFuzzyMatch(Long customerId, Long productId, String coreProductName);

    void createBid(CustomerProduct dto);

    void editBid(CustomerProduct dto);

    MyPage<CustomerProductResDTO> list(CustomerProductReqDTO dto);

    CustomerProductResDTO get(Long dto);

    void delete(Long id);

    Map<String, Object> importFromExcel(MultipartFile file);
}
