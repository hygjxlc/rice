package com.bjdx.rice.admin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Table;

@Data
@Table(name = "permission")
public class Permission {
    /**
     * 权限ID
     */
    @ApiModelProperty(value = "权限ID")
    private Long id;
    /**
     * 权限编码
     */
    @ApiModelProperty(value = "权限编码")
    private String permissionCode;
    /**
     * 权限名称
     */
    @ApiModelProperty(value = "权限名称")
    private String permissionName;
    /**
     * 权限类型
     */
    @ApiModelProperty(value = "权限类型")
    private String type;
}
