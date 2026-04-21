package com.bjdx.rice.business.entity;

import java.util.Date;
import java.io.Serializable;

import com.bjdx.rice.business.dto.order.OrderMark;
import com.bjdx.rice.business.dto.order.OrderStatus;
import com.bjdx.rice.business.dto.order.OrderType;
import lombok.Data;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * 订单主表(Orders)实体类
 *
 * @author makejava
 * @since 2025-12-13 16:20:18
 */
@Data
@Entity
@Table(name = "orders")
public class Orders implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @ApiModelProperty("订单ID")
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 订单编号，如 ORD202512130001
     */
    @ApiModelProperty("订单编号，如 ORD202512130001")
    @Column(name = "order_no")
    private String orderNo;

    /**
     * 订单类型: 电子订单, 手工订单, 微信订单
     */
    @ApiModelProperty("订单类型: 电子订单, 手工订单, 微信订单")
    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    /**
     * 客户名称
     */
    @ApiModelProperty("客户名称")
    @Column(name = "customer_name")
    private String customerName;

    /**
     * 客户ID
     */
    @ApiModelProperty("客户ID")
    @Column(name = "customer_id")
    private Long customerId;
    /**
     * 联系人
     */
    @ApiModelProperty("联系人")
    @Column(name = "contacts")
    private String contacts;

    /**
     * 联系电话
     */
    @ApiModelProperty("联系电话")
    @Column(name = "phone")
    private String phone;

    /**
     * 配送地址
     */
    @ApiModelProperty("配送地址")
    @Column(name = "delivery_address")
    private String deliveryAddress;
    /**
     * 计划交期
     */
    @ApiModelProperty("计划交期")
    @Column(name = "delivery_date")
    private Date deliveryDate;

    /**
     * 订单状态: 待处理, 已确认, 已发货, 已完成
     */
    @ApiModelProperty("订单状态: 待处理, 已确认, 已发货, 已完成")
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    /**
     * 订单标记
     */
    @ApiModelProperty("订单标记")
    @Column(name = "mark")
    @Enumerated(EnumType.STRING)
    private OrderMark mark;

    /**
     * 订单备注
     */
    @ApiModelProperty("订单备注")
    @Column(name = "remark")
    private String remark;

    /**
     * 下单时间
     */
    @ApiModelProperty("下单时间")
    @Column(name = "order_at")
    private Date orderAt;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @Column(name = "created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @Column(name = "updated_at")
    private Date updatedAt;
}
