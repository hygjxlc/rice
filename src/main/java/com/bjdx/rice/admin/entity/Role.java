package com.bjdx.rice.admin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Table(name = "sys_role")
public class Role {
    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID")
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    private String roleName;
    /**
     * 角色描述
     */
    @ApiModelProperty(value = "角色描述")
    private String description;
    /**
     * 权限ID列表
     */
    @ApiModelProperty(value = "权限ID列表")
    private List<Long> permissionIds; // 用于接收前端传入的权限ID列表
}
