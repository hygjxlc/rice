package com.bjdx.rice.business.dto.customerProduct;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CustomerProductResDTO {
    private Long id;
    @ApiModelProperty(value = "客户ID")
    private Long unitId;
    @ApiModelProperty("客户名称")
    private String unitName;
    @ApiModelProperty("客户编号")
    private String unitCode;
    @ApiModelProperty(value = "产品ID")
    private Long productId;
    @ApiModelProperty("产品名称")
    private String productName;
    @ApiModelProperty("产品编号")
    private String productCode;
    @ApiModelProperty(value = "单价")
    private BigDecimal price;
    @ApiModelProperty("有效开始时间")
    private Date startTime;

    @ApiModelProperty("有效结束时间")
    private Date endTime;
}