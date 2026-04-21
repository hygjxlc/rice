package com.bjdx.rice.business.entity;

import java.util.Date;
import java.io.Serializable;
import lombok.Data;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * 订单商品明细表(OrderItems)实体类
 *
 * @author makejava
 * @since 2025-12-13 16:20:18
 */
@Data
@Entity
@Table(name = "order_items")
public class OrderItems implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 明细ID
     */
    @ApiModelProperty("明细ID")
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;
    /**
     * 关联订单ID
     */
    @ApiModelProperty("关联订单ID")
    @Column(name = "order_id")
    private Long orderId;
    /**
     * 商品名称
     */
    @ApiModelProperty("商品名称")
    @Column(name = "product_name")
    private String productName;
    /**
     * 商品id
     */
    @ApiModelProperty("商品id")
    @Column(name = "product_id")
    private Long productId;
    /**
     * 商品编号
     */
    @ApiModelProperty("商品编号")
    @Column(name = "product_no")
    private String productNo;
    /**
     * 规格（如：5kg/袋）
     */
    @ApiModelProperty("规格（如：5kg/袋）")
    @Column(name = "specification")
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
    /**
     * 单价（元）
     */
    @ApiModelProperty("单价（元）")
    @Column(name = "unit_price")
    private Double unitPrice;
    /**
     * 小计（自动计算：数量 × 单价）
     */
    @ApiModelProperty("小计（自动计算：数量 × 单价）")
    @Column(name = "subtotal")
    private Double subtotal;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @Column(name = "created_at")
    private Date createdAt;



}

