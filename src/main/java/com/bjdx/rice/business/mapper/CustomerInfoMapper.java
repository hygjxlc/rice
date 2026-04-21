package com.bjdx.rice.business.mapper;

import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.business.dto.customer.CustomerReqDTO;
import com.bjdx.rice.business.entity.CustomerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.Collection;
import java.util.List;

@Repository
public interface CustomerInfoMapper extends BaseMapper<CustomerInfo>, MySqlMapper<CustomerInfo> {

    List<CustomerInfo> query(@Param("customer") CustomerReqDTO customer);

    CustomerInfo selectByUnitName(String unitName);

    // CustomerInfoMapper.java
    List<String> selectUnitNamesIn(@Param("names") Collection<String> names);
    List<String> selectUnitCodesIn(@Param("codes") Collection<String> codes);
    void insertBatch(@Param("list") List<CustomerInfo> list);

    List<DropDownDTO> getAllCustomers(String name);

    CustomerInfo getCustomerByName(String customerName);

    Long getIdByName(String name);

    // 批量更新客户信息
    void updateBatch(@Param("list") List<CustomerInfo> list);
    // 根据多个单位名称批量查询客户信息
    List<CustomerInfo> selectByUnitNames(@Param("unitNames") List<String> unitNames);
}
