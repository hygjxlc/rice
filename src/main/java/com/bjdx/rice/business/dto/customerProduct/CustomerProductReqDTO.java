package com.bjdx.rice.business.dto.customerProduct;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class CustomerProductReqDTO {
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

    @ApiModelProperty("产品名称")
    private String productName;

    @ApiModelProperty("有效开始时间")
    private Date startTime;

    @ApiModelProperty("有效结束时间")
    private Date endTime;
}
