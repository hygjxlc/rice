package com.bjdx.rice.business.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_info")
@Data
public class CustomerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_code", nullable = false, unique = true)
    @ApiModelProperty("客户编码")
    private String unitCode;

    @Column(name = "unit_name", nullable = false)
    @ApiModelProperty("客户名称")
    private String unitName;

    @Enumerated(EnumType.STRING)
    @Column(name = "disabled_flag", nullable = false)
    @ApiModelProperty("禁用标识")
    private DisabledFlag disabledFlag = DisabledFlag.否;

    @Column(name = "unit_alias")
    @ApiModelProperty("客户别名")
    private String unitAlias;

    @Column(name = "contact_person")
    @ApiModelProperty("联系人")
    private String contactPerson;

    @Column(name = "contact_phone")
    @ApiModelProperty("联系电话")
    private String contactPhone;

    @Column(name = "region")
    @ApiModelProperty("地区")
    private String region;

    @Column(name = "address")
    @ApiModelProperty("地址")
    private String address;

    @Column(name = "mobile_phone")
    @ApiModelProperty("手机号码")
    private String mobilePhone;

    @Column(name = "remarks")
    @ApiModelProperty("备注")
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    @ApiModelProperty("创建人")
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    @ApiModelProperty("更新人")
    private String updatedBy;

    public enum DisabledFlag {
        是, 否
    }
}
