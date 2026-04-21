package com.bjdx.rice.business.dto.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("订单分页查询请求")
public class OrderPageQueryRequest {
    
    @ApiModelProperty("页码，默认1")
    private Integer pageNum = 1;
    
    @ApiModelProperty("每页大小，默认10")
    private Integer pageSize = 10;
    
    @ApiModelProperty("订单状态")
    private String status;
    
    @ApiModelProperty("关键字（订单号、客户名称、电话）")
    private String keyword;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("订单创建开始时间")
    private Date startTime;

    @ApiModelProperty("订单创建结束时间")
    private Date endTime;
}
