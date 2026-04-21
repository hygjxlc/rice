package com.bjdx.rice.business.dto.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("用友接口同步请求")
public class YongyouSyncRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("供应商名称")
    private String supperName;

    @ApiModelProperty("员工名称")
    private String empName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("仓库名称")
    private String storName;

    @ApiModelProperty("明细列表")
    private List<YongyouDetail> details;

    @Data
    @ApiModel("用友明细项")
    public static class YongyouDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("仓库名称 成品库")
        private String storName = "成品库";

        @ApiModelProperty("商品名称")
        private String prod_Name;

        @ApiModelProperty("商品编号")
        private Integer prod_Number;

        @ApiModelProperty("商品单位")
        private String prod_DW;

        @ApiModelProperty("商品价格")
        private Double prod_Price;
    }
}
