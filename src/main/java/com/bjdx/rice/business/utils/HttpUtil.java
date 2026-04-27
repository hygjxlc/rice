package com.bjdx.rice.business.utils;

import com.alibaba.fastjson.JSON;
import com.bjdx.rice.business.dto.HttpResponseDTO;
import com.bjdx.rice.business.exception.MyException;
import com.bjdx.rice.business.exception.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class HttpUtil {

    public static HttpResponseDTO doPost(String urlStr, String type, Object params, Map<String, String> header, RequestConfig requestConfig) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        HttpEntityEnclosingRequestBase http = null;
        if (type.equals("POST")) {
            http = new HttpPost(urlStr);
        } else if (type.equals("PUT")) {
            http = new HttpPut(urlStr);
        } else if (type.equals("PATCH")) {
            http = new HttpPatch(urlStr);
        } else {
            throw new MyException("http请求类型不正确");
        }


        if(requestConfig==null) {
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(30000)
                    .setConnectionRequestTimeout(10000)
                    .setConnectTimeout(10000)
                    .build();
        }

        http.setConfig(requestConfig);

        CloseableHttpResponse response = null;

        HttpResponseDTO httpResponseDTO = new HttpResponseDTO();
        try {
            if (!MapUtils.isEmpty(header)) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    http.addHeader(entry.getKey(), entry.getValue());
                }
            }

            StringEntity entity = new StringEntity(JSON.toJSONString(params), StandardCharsets.UTF_8);
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            http.setEntity(entity);

            //发送post请求
            response = closeableHttpClient.execute(http);
            if (response == null || response.getStatusLine() == null) {
                httpResponseDTO.setHttpCode(500);
                return  httpResponseDTO;
//                throw new MyException("发送http请求失败，返回结果为空！");
            }
            int code = response.getStatusLine().getStatusCode();
            //得到请求结果
            HttpEntity entityRes = response.getEntity();
            String responsetData = "";
            if (entityRes != null) {
                responsetData = EntityUtils.toString(entityRes, StandardCharsets.UTF_8);
            }
//            if (code != 200) {
//                throw new MyException("http请求失败:" + responsetData);
//            }
            httpResponseDTO.setResponseData(responsetData);
            httpResponseDTO.setHttpCode(code);
            return httpResponseDTO;
        } catch (Exception e) {
            httpResponseDTO.setHttpCode(500);
            throw new MyException(ResponseCode.HTTP_FAIL.code, e.getMessage());
        } finally {
            try {
                // 关闭连接释放资源
                if (response != null) {
                    response.close();
                }
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public static HttpResponseDTO doGet(String urlStr, String type, Map<String, String> header,RequestConfig requestConfig) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        HttpRequestBase http = null;
        if (type.equals("GET")) {
            http = new HttpGet(urlStr);
        } else if (type.equals("DELETE")) {
            http = new HttpDelete(urlStr);
        } else {
            throw new MyException("http请求类型不正确");
        }

        if(requestConfig==null) {
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(30000)
                    .setConnectionRequestTimeout(10000)
                    .setConnectTimeout(10000)
                    .build();
        }
        http.setConfig(requestConfig);

        HttpResponseDTO httpResponseDTO = new HttpResponseDTO();
        CloseableHttpResponse response = null;
        try {
            if (!MapUtils.isEmpty(header)) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    http.addHeader(entry.getKey(), entry.getValue());
                }
            }

            //发送请求
            response = closeableHttpClient.execute(http);
            if (response == null || response.getStatusLine() == null) {
                httpResponseDTO.setHttpCode(500);
                return  httpResponseDTO;
//                throw new MyException("发送http请求失败，返回结果为空！");
            }
            int code = response.getStatusLine().getStatusCode();
//            得到请求结果
            HttpEntity entityRes = response.getEntity();
            String responsetData = "";
            if (entityRes != null) {
                responsetData = EntityUtils.toString(entityRes, StandardCharsets.UTF_8);

            }
            httpResponseDTO.setHttpCode(code);
            httpResponseDTO.setResponseData(responsetData);
            return httpResponseDTO;
        } catch (Exception e) {
            httpResponseDTO.setHttpCode(500);
            throw new MyException(ResponseCode.HTTP_FAIL.code, e.getMessage());
        } finally {
            try {
                // 关闭连接释放资源
                if (response != null) {
                    response.close();
                }
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }


}
