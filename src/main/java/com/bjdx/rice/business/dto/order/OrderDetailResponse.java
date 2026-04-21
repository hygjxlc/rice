package com.bjdx.rice.business.dto.order;

import com.bjdx.rice.business.entity.Orders;
import com.bjdx.rice.business.entity.OrderItems;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("订单详情响应")
public class OrderDetailResponse {

    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("订单类型")
    private String orderType;

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

    @ApiModelProperty("商品种类")
    private Integer productCount;

    @ApiModelProperty("总金额")
    private Double totalAmount;


    @ApiModelProperty("订单状态")
    private String status;

    @ApiModelProperty("订单标记")
    private String mark;

    @ApiModelProperty("订单备注")
    private String remark;

    @ApiModelProperty("下单时间")
    private Date orderAt;

    @ApiModelProperty("创建时间")
    private java.util.Date createdAt;

    @ApiModelProperty("更新时间")
    private java.util.Date updatedAt;

    @ApiModelProperty("订单商品明细")
    private List<OrderItems> items;

    public static OrderDetailResponse from(Orders order, List<OrderItems> items) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setOrderType(order.getOrderType().name());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerId(order.getCustomerId());
        response.setPhone(order.getPhone());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setStatus(order.getStatus().name());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setOrderAt(order.getOrderAt());
        response.setContacts(order.getContacts());
        response.setMark(order.getMark().name());
        response.setDeliveryDate(order.getDeliveryDate());

        if (items != null && !items.isEmpty()) {
            response.setItems(items);
            response.setProductCount(items.size());
            response.setTotalAmount(items.stream().mapToDouble(OrderItems::getSubtotal).sum());
        }


        return response;
    }
}
