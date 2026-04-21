package com.bjdx.rice.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RoleListReqDTO {
    @ApiModelProperty("页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty("每页大小，默认10")
    private Integer pageSize = 10;
    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    private String roleName;

}
