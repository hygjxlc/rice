package com.bjdx.rice.business.dto.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    /**
     * 商品ID
     */
    @ApiModelProperty("商品ID")
    private Long id;
    @ApiModelProperty("商品编码")
    private String productCode; // 可为空，由系统生成

    @NotBlank(message = "商品名称不能为空")
    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品类别")
    private String productType; // 枚举值校验可在 Service 做

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("价格")
    private BigDecimal price;
    @ApiModelProperty("品牌")
    private String brand;

    @ApiModelProperty("规格")
    private String specification;
    @ApiModelProperty("指标说明")
    private String indicatorDesc;
    @ApiModelProperty("备注")
    private String remarks;

}