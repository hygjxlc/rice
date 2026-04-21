package com.bjdx.rice.business.dto.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @ApiModelProperty("订单ID")
    private Long id;
    @ApiModelProperty("订单状态:  '待处理','已确认','已发货','已完成'")
    private String status;
}
