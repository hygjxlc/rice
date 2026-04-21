package com.bjdx.rice.business.excel;

import lombok.Data;

@Data
public class SalesOrderHeader {
    private String documentDate;     // 单据日期
    private String documentNo;       // 单据编号
    private String preparedBy;       // 制单人
    private String customer;         // 往来单位
    private String accountTaxNo;     // 账号税号
    private String handler;          // 经手人
    private String summary;          // 单据摘要
}