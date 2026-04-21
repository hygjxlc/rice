package com.bjdx.rice.business.controller;

import com.bjdx.rice.business.config.VectorSearchProperties;
import com.bjdx.rice.business.dto.ResponseObj;
import com.bjdx.rice.business.service.vector.VectorSearchService;
import com.bjdx.rice.business.service.vector.VectorSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 向量检索管理控制器
 * 提供向量检索功能的管理接口
 * 
 * @author Rice System
 */
@RestController
@RequestMapping("/vector-admin")
@Api(tags = "向量检索管理", description = "向量检索功能的管理接口")
public class VectorAdminController {

    private static final Logger logger = LoggerFactory.getLogger(VectorAdminController.class);

    @Autowired
    private VectorSearchProperties vectorProperties;

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private VectorSyncService vectorSyncService;

    /**
     * 获取向量检索配置状态
     */
    @GetMapping("/status")
    @ApiOperation("获取向量检索状态")
    public ResponseObj<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", vectorProperties.getSearch().isEnabled());
        status.put("similarityThreshold", vectorProperties.getSearch().getSimilarityThreshold());
        status.put("maxResults", vectorProperties.getSearch().getMaxResults());
        status.put("logMatches", vectorProperties.getSearch().isLogMatches());
        status.put("modelType", vectorProperties.getEmbedding().getModelType());
        status.put("databaseType", vectorProperties.getDatabase().getType());
        status.put("available", vectorSearchService.isAvailable());
        return ResponseObj.success().put(status);
    }

    /**
     * 触发全量重建客户向量
     */
    @PostMapping("/rebuild/customers")
    @ApiOperation("全量重建客户向量")
    public ResponseObj<String> rebuildCustomerVectors() {
        if (!vectorProperties.getSearch().isEnabled()) {
            return ResponseObj.error("向量检索功能未启用");
        }

        try {
            vectorSyncService.rebuildAllCustomers();
            return ResponseObj.success("客户向量重建任务已启动");
        } catch (Exception e) {
            logger.error("Failed to rebuild customer vectors", e);
            return ResponseObj.error("重建失败: " + e.getMessage());
        }
    }

    /**
     * 触发全量重建商品向量
     */
    @PostMapping("/rebuild/products")
    @ApiOperation("全量重建商品向量")
    public ResponseObj<String> rebuildProductVectors() {
        if (!vectorProperties.getSearch().isEnabled()) {
            return ResponseObj.error("向量检索功能未启用");
        }

        try {
            vectorSyncService.rebuildAllProducts();
            return ResponseObj.success("商品向量重建任务已启动");
        } catch (Exception e) {
            logger.error("Failed to rebuild product vectors", e);
            return ResponseObj.error("重建失败: " + e.getMessage());
        }
    }

    /**
     * 测试向量检索
     */
    @GetMapping("/test/customer")
    @ApiOperation("测试客户向量检索")
    public ResponseObj<Map<String, Object>> testCustomerSearch(
            @RequestParam String name,
            @RequestParam(defaultValue = "0.75") float threshold) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return ResponseObj.error("向量检索功能未启用");
        }

        try {
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchCustomer(
                    name, 3, threshold);

            Map<String, Object> response = new HashMap<>();
            if (result != null && result.isSuccess()) {
                response.put("found", true);
                response.put("id", result.getId());
                response.put("name", result.getName());
                response.put("score", result.getScore());
            } else {
                response.put("found", false);
            }
            return ResponseObj.success().put(response);
        } catch (Exception e) {
            logger.error("Vector search test failed", e);
            return ResponseObj.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试商品向量检索
     */
    @GetMapping("/test/product")
    @ApiOperation("测试商品向量检索")
    public ResponseObj<Map<String, Object>> testProductSearch(
            @RequestParam String name,
            @RequestParam(defaultValue = "0.75") float threshold) {
        if (!vectorProperties.getSearch().isEnabled()) {
            return ResponseObj.error("向量检索功能未启用");
        }

        try {
            VectorSearchService.VectorSearchResult result = vectorSearchService.searchProduct(
                    name, 3, threshold);

            Map<String, Object> response = new HashMap<>();
            if (result != null && result.isSuccess()) {
                response.put("found", true);
                response.put("id", result.getId());
                response.put("name", result.getName());
                response.put("score", result.getScore());
            } else {
                response.put("found", false);
            }
            return ResponseObj.success().put(response);
        } catch (Exception e) {
            logger.error("Vector search test failed", e);
            return ResponseObj.error("测试失败: " + e.getMessage());
        }
    }
}
