package com.bjdx.rice.business.utils;

import com.alibaba.fastjson.JSONObject;
import com.bjdx.rice.business.dto.HttpResponseDTO;
import com.bjdx.rice.business.exception.MyException;
import org.apache.commons.collections.MapUtils;
import org.apache.http.client.config.RequestConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ymh
 * @date 2025/2/28 9:38
 */
public class ChatUtils {

//
//    /**
//     * @Description:发送请求给对话大模型
//     * @author hongkai
//     * @date 2025/3/5 14:58
//     */
//    public static String call(String url,String apiKey,String model, List<Map> messages, Map params) {
//
//        Map<String, String> header = new HashMap<>();
//        header.put("Content-Type", "application/json");
//        header.put("Authorization", "Bearer " + apiKey);
//
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(30000000)
//                .setConnectionRequestTimeout(30000000)
//                .setConnectTimeout(30000000)
//                .build();
//
//
//        //构建参数
//        Map json =  new HashMap();
//        json.put("model", model);
//        json.put("messages", messages);
//        if(!MapUtil.isEmpty(params)) {
//            json.putAll(params);
//        }
//
//        HttpResponseDTO httpResponseDTO = HttpUtil.doPost(url, "POST", json, header, requestConfig);
//        if(httpResponseDTO.getHttpCode()!=200) {
//            throw new MyException("大模型调用失败:" + httpResponseDTO.getResponseData());
//        }
//        return httpResponseDTO.getResponseData();
//    }


    /**
     * @Description:发送请求给对话大模型
     * @author hongkai
     * @date 2025/3/5 14:58
     */
    public static String call(String url,String apiKey,String model, Map json, Map params) {

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Authorization", "Bearer " + apiKey);

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000000)
                .setConnectionRequestTimeout(30000000)
                .setConnectTimeout(30000000)
                .build();


        HttpResponseDTO httpResponseDTO = HttpUtil.doPost(url, "POST", json, header, requestConfig);
        if(httpResponseDTO.getHttpCode()!=200) {
            throw new MyException("大模型调用失败:" + httpResponseDTO.getResponseData());
        }
        return httpResponseDTO.getResponseData();
    }

    /**
     * @Description:获取大模型对话结果中的AI回答内容
     * @author hongkai
     * @date 2025/3/5 14:57
     */
    public static String getAiAnswer(JSONObject json) {
        if(MapUtils.isEmpty(json)) {
            return "";
        }
        List choices = json.getJSONArray("choices");
        JSONObject resultMessage = (JSONObject) choices.get(0);
        String aiAnswer = resultMessage.getJSONObject("message").getString("content");
        return aiAnswer;
    }




}
