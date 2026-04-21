package com.bjdx.rice.business.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportUtil {
    // 构建返回结果
    public static Map<String, Object> buildResult(int total, int success, int fail, List<String> errors) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("fail", fail);
        result.put("msg", errors);
        return result;
    }
}
