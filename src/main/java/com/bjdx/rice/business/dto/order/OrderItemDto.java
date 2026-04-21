package com.bjdx.rice.business.dto.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("订单商品项")
public class OrderItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("商品编号")
    private String productNo;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品ID")
    private Long productId;
    
    @ApiModelProperty("规格（如：5kg/袋）")
    private String specification;
    
    @ApiModelProperty("数量")
    private Double quantity;
    
    @ApiModelProperty("单位（如：袋、件、箱）")
    private String unit;
    
    @ApiModelProperty("单价（元）")
    private Double unitPrice;
}
