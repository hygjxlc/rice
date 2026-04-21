package com.bjdx.rice.business.dto.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("订单分页查询响应")
public class OrderPageResponse {
    
    @ApiModelProperty("订单ID")
    private Long id;
    
    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("商品种类")
    private Integer productCount;

    @ApiModelProperty("计划交期")
    private Date deliveryDate;
    
    @ApiModelProperty("订单状态")
    private String status;

    @ApiModelProperty("订单类型")
    private String orderType;

    @ApiModelProperty("订单标记")
    private String mark;
    
    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("客户id")
    private Long customerId;

    @ApiModelProperty("联系人")
    private String contacts;
    
    @ApiModelProperty("联系电话")
    private String phone;
    
    @ApiModelProperty("商品信息列表")
    private List<ProductInfo> products;
    
    @ApiModelProperty("总金额")
    private Double totalAmount;
    
    @ApiModelProperty("下单时间")
    private Date orderAt;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("配送地址")
    private String deliveryAddress;
    
    @Data
    @ApiModel("商品信息")
    public static class ProductInfo {
        @ApiModelProperty("商品名称")
        private String productName;

        @ApiModelProperty("商品id")
        private Long productId;
        
        @ApiModelProperty("规格")
        private String specification;

        /**
         * 数量
         */
        @ApiModelProperty("数量")
        @Column(name = "quantity")
        private Double quantity;
        /**
         * 单位（如：袋、件、箱）
         */
        @ApiModelProperty("单位（如：袋、件、箱）")
        @Column(name = "unit")
        private String unit;
    }
}
