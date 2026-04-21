package com.bjdx.rice.business.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description:HttpUtil返回体
 * @author hongkai
 * @date 2024/2/21 10:20
 */
@Data
@ApiModel
public class HttpResponseDTO {

    @ApiModelProperty("http响应码")
    private Integer httpCode;

    @ApiModelProperty("响应体")
    private String responseData;
}
