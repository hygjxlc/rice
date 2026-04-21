package com.bjdx.rice.business.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "customer_product")
public class CustomerProduct {
    @Id
    private Long id;
    @ApiModelProperty(value = "客户ID")
    private Long customerId;
    @ApiModelProperty(value = "产品ID")
    private Long productId;
    @ApiModelProperty(value = "商品名称（Excel原始名称）")
    private String productName;
    @ApiModelProperty(value = "单价")
    private BigDecimal price;
    @ApiModelProperty(value = "有效开始时间")
    private Date startTime;
    @ApiModelProperty(value = "有效结束时间")
    private Date endTime;
}
