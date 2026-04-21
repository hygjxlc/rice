package com.bjdx.rice.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserListReqDTO {
    @ApiModelProperty("页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty("每页大小，默认10")
    private Integer pageSize = 10;
    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    private String username;
    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String nickname;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer status;
    /**
     * 角色
     */
    @ApiModelProperty(value = "角色ID")
    private Integer roleId;
}
