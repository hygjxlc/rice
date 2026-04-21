package com.bjdx.rice.business.dto.customer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class CustomerReqDTO {
    /**
     * 页数
     */
    @ApiModelProperty("页数")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @ApiModelProperty("每页数量")
    private Integer pageSize = 10;

    /**
     * 单位名称
     */
    @ApiModelProperty("单位名称")
    private String unitName;

    /**
     * 联系人
     */
    @ApiModelProperty("联系人")
    private String contactPerson;

    /**
     * 联系电话
     */
    @ApiModelProperty("联系电话")
    private String contactPhone;

    /**
     * 创建开始时间
     */
    @ApiModelProperty("创建开始时间")
    private Date startCreatedAt;
    /**
     * 创建结束时间
     */
    @ApiModelProperty("创建结束时间")
    private Date endCreatedAt;
}
