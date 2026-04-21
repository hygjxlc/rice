// com.bjdx.rice.business.service.CustomerImportListener.java
package com.bjdx.rice.business.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.bjdx.rice.business.entity.CustomerInfo;
import com.bjdx.rice.business.exception.MyException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class CustomerImportListener extends AnalysisEventListener<Map<Integer, String>> {

    // 表头别名定义（JDK 8 兼容）
    private static final Set<String> UNIT_CODE_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("单位编码", "客户编码"))
    );
    private static final Set<String> UNIT_NAME_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("单位名称", "客户名称"))
    );
    private static final Set<String> DISABLED_FLAG_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("停用标志", "是否停用"))
    );
    private static final Set<String> UNIT_ALIAS_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("单位别名", "客户别名"))
    );
    private static final Set<String> CONTACT_PERSON_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singletonList("联系人"))
    );
    private static final Set<String> CONTACT_PHONE_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singletonList("联系电话"))
    );
    private static final Set<String> REGION_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singletonList("地区"))
    );
    private static final Set<String> ADDRESS_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("联系地址", "地址"))
    );
    private static final Set<String> MOBILE_PHONE_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singletonList("移动电话"))
    );
    private static final Set<String> REMARKS_HEADERS = Collections.unmodifiableSet(
            new HashSet<>(Collections.singletonList("备注"))
    );

    // 列索引
    private Integer unitCodeIdx;
    private Integer unitNameIdx;
    private Integer disabledFlagIdx;
    private Integer unitAliasIdx;
    private Integer contactPersonIdx;
    private Integer contactPhoneIdx;
    private Integer regionIdx;
    private Integer addressIdx;
    private Integer mobilePhoneIdx;
    private Integer remarksIdx;

    private final List<CustomerInfo> customers = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if (headMap == null || headMap.isEmpty()) {
            throw new MyException("Excel 表头为空");
        }

        Map<String, Integer> headerToIndex = new HashMap<>();
        for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
            String header = entry.getValue();
            if (header != null) {
                headerToIndex.put(header.trim(), entry.getKey());
            }
        }

        unitCodeIdx = findHeaderIndex(headerToIndex, UNIT_CODE_HEADERS);
        unitNameIdx = findHeaderIndex(headerToIndex, UNIT_NAME_HEADERS);
        disabledFlagIdx = findHeaderIndex(headerToIndex, DISABLED_FLAG_HEADERS);
        unitAliasIdx = findHeaderIndex(headerToIndex, UNIT_ALIAS_HEADERS);
        contactPersonIdx = findHeaderIndex(headerToIndex, CONTACT_PERSON_HEADERS);
        contactPhoneIdx = findHeaderIndex(headerToIndex, CONTACT_PHONE_HEADERS);
        regionIdx = findHeaderIndex(headerToIndex, REGION_HEADERS);
        addressIdx = findHeaderIndex(headerToIndex, ADDRESS_HEADERS);
        mobilePhoneIdx = findHeaderIndex(headerToIndex, MOBILE_PHONE_HEADERS);
        remarksIdx = findHeaderIndex(headerToIndex, REMARKS_HEADERS);

        if (unitCodeIdx == null) {
            throw new MyException("未找到单位编码列");
        }
        if (unitNameIdx == null) {
            throw new MyException("未找到单位名称列");
        }
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        long rowIndex = context.readRowHolder().getRowIndex() + 1;

        try {
            String unitCode = getStringValue(data, unitCodeIdx);
            String unitName = getStringValue(data, unitNameIdx);

            // 单位名称不能为空
            if (StringUtils.isBlank(unitName)) {
                errors.add("第 " + rowIndex + " 行: 单位名称不能为空");
                return;
            }

            CustomerInfo customer = new CustomerInfo();
            customer.setUnitCode(unitCode); // 可以为null或空，后续会自动生成
            customer.setUnitName(unitName);

            // 停用标志处理
            if (disabledFlagIdx != null) {
                String flag = getStringValue(data, disabledFlagIdx);
                customer.setDisabledFlag("是".equals(flag) ?
                        CustomerInfo.DisabledFlag.是 : CustomerInfo.DisabledFlag.否);
            } else {
                customer.setDisabledFlag(CustomerInfo.DisabledFlag.否); // 默认不禁用
            }

            customer.setUnitAlias(getStringValue(data, unitAliasIdx));
            customer.setContactPerson(getStringValue(data, contactPersonIdx));
            customer.setContactPhone(getStringValue(data, contactPhoneIdx));
            customer.setRegion(getStringValue(data, regionIdx));
            customer.setAddress(getStringValue(data, addressIdx));
            customer.setMobilePhone(getStringValue(data, mobilePhoneIdx));
            customer.setRemarks(getStringValue(data, remarksIdx));

            customers.add(customer);

        } catch (Exception e) {
            errors.add("第 " + rowIndex + " 行: " + e.getMessage());
        }
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // no-op
    }

    private String getStringValue(Map<Integer, String> data, Integer index) {
        if (index == null) return "";
        String val = data.get(index);
        return val == null ? "" : val.trim();
    }

    private Integer findHeaderIndex(Map<String, Integer> headerToIndex, Set<String> aliases) {
        for (String alias : aliases) {
            if (headerToIndex.containsKey(alias)) {
                return headerToIndex.get(alias);
            }
        }
        return null;
    }

    // Getters
    public List<CustomerInfo> getCustomers() {
        return customers;
    }

    public List<String> getErrors() {
        return errors;
    }
}