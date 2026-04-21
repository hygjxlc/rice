package com.bjdx.rice.admin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Table(name = "sys_user")
public class User {
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
     * 密码
     */
    @ApiModelProperty("密码")
    private String password;
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
    /**
     * 用于接收前端传入的角色ID列表
     */
    private List<Long> roleIds; // 用于接收前端传入的角色ID列表
}
