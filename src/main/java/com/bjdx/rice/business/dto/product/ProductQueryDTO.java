package com.bjdx.rice.business.dto.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ProductQueryDTO {
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
     * 商品编码
     */
    @ApiModelProperty("商品编码")
    private String productCode;
    /**
     * 商品名称
     */
    @ApiModelProperty("商品名称")
    private String productName;
    /**
     * 商品类别
     */
    @ApiModelProperty("商品类别")
    private String productType;
    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createdBy;
    /**
     * 创建开始时间
     */
    @ApiModelProperty("创建开始时间")
    private Date startCreatedTime;
    /**
     * 创建结束时间
     */
    @ApiModelProperty("创建结束时间")
    private Date endCreatedTime;
}