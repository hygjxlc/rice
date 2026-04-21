package com.bjdx.rice.business.dto.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ymh
 * @date 2025/12/13 16:56
 */
@Data
public class UpdateOrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("客户id")
    private Long customerId;

    @ApiModelProperty("联系人")
    private String contacts;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("配送地址")
    private String deliveryAddress;

    @ApiModelProperty("计划交期")
    private Date deliveryDate;

    @ApiModelProperty("订单备注")
    private String remark;

    @ApiModelProperty("订单类型: 电子订单, 手工订单, 微信订单")
    private String orderType;

    @ApiModelProperty("订单商品明细")
    private List<OrderItemDto> items;
}
