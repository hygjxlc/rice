package com.bjdx.rice.business.dto;



import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

@Data
@ApiModel
public class BaseDTO implements Serializable {

    @Min(message = "页最小值为1",value = 1)
    @ApiModelProperty("页")
    public Integer pageNum =1;

    @Min(message = "页大小最小值为1",value = 1)
    @ApiModelProperty("页大小")
    public Integer pageSize = 10;

//    @ApiModelProperty("排序字段")
//    public String orderField;
//
//    @ApiModelProperty("排序方式")
//    public String order;
}
