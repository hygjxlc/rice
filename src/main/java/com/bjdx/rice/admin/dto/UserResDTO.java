package com.bjdx.rice.admin.dto;

import com.bjdx.rice.admin.entity.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class UserResDTO {
    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @ApiModelProperty("用户ID")
    private Long id;
    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;
    /**
     * 昵称
     */
    @ApiModelProperty("昵称")
    private String nickname;
    /**
     * 手机
     */
    @ApiModelProperty("手机")
    private String phone;
    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    private String email;
    /**
     * 状态
     */
    @ApiModelProperty("状态 1-启用, 0-禁用")
    private Integer status; // 1-启用, 0-禁用
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;
    // 角色列表
    @ApiModelProperty("角色列表")
    private List<Role> roles;
    // 或只查询角色ID和名称
    @ApiModelProperty("角色名称")
    private List<String> roleNames;
    @ApiModelProperty("角色ID")
    private List<Long> roleIds;
}
