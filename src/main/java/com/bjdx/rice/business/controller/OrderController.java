package com.bjdx.rice.business.controller;

import com.bjdx.rice.business.dto.order.*;
import com.bjdx.rice.business.dto.ResponseObj;
import com.bjdx.rice.business.entity.Orders;
import com.bjdx.rice.business.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author ymh
 * @date 2025/12/13 16:29
 */
@RestController
@Api(tags = "订单管理")
@RequestMapping("/order")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Resource
    private OrderService orderService;


    /**
     * 创建订单
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation("创建订单")
    public ResponseObj createOrder(@RequestBody CreateOrderRequest request) {
        Orders order = orderService.createOrder(request);
        return ResponseObj.success(order.getId());
    }
    /**
     * 修改订单
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ApiOperation("修改订单")
    public ResponseObj updateOrder(@RequestBody UpdateOrderRequest  request) {
        orderService.updateOrder(request);
        return ResponseObj.success();
    }
    /**
     * 删除订单
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiOperation("删除订单")
    public ResponseObj deleteOrder(@RequestBody List<Long> ids) {
        for (Long id : ids)
        {
            orderService.deleteOrder(id);
        }
        return ResponseObj.success();
    }
    /**
     * 订单详情
     */
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    @ApiOperation("订单详情")
    public ResponseObj queryOrder(@PathVariable("id") Long id) {
        return ResponseObj.success().put(orderService.getOrderDetail(id));
    }
    /**
     * 订单列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ApiOperation("订单列表")
    public ResponseObj queryOrderList(@RequestBody OrderPageQueryRequest request) {
        return ResponseObj.success().put(orderService.queryOrderList(request));
    }

    /**
     * 完成订单
     */
    @RequestMapping(value = "/finishOrder/{id}", method = RequestMethod.GET)
    @ApiOperation("完成订单")
    public ResponseObj finishOrder(@PathVariable("id") Long id) {
        orderService.finishOrder(id);
        return ResponseObj.success();
    }

    /**
     * 同步到用友
     */
    @RequestMapping(value = "/syncToYongyou/{id}", method = RequestMethod.GET)
    @ApiOperation("同步到用友")
    public ResponseObj syncToYongyou(@PathVariable Long id) {
        orderService.syncToYongyou(id);
        return ResponseObj.success();
    }

    /**
     * 批量同步
     */
    @RequestMapping(value = "/batchSync", method = RequestMethod.POST)
    @ApiOperation("批量同步")
    public ResponseObj batchSync(@RequestBody List<Long> ids) {
        orderService.batchSync(ids);
        return ResponseObj.success();
    }

    /**
     * 修改订单状态
     */
    @RequestMapping(value = "/updateOrderStatus", method = RequestMethod.POST)
    @ApiOperation("修改订单状态")
    public ResponseObj updateOrderStatus(@RequestBody UpdateOrderStatusRequest request) {
        orderService.updateOrderStatus(request);
        return ResponseObj.success();
    }

    /**
     * 订单识别
     */
    @RequestMapping(value = "/orderRecognition", method = RequestMethod.POST)
    @ApiOperation("订单识别")
    public ResponseObj<CreateOrderRequest> orderRecognition(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "order_type", required = false) String order_type,
            @RequestParam(value = "type", required = false) String type) {
        // 调试日志：输出接收到的所有参数
        logger.info("【OrderController】orderRecognition接口接收参数 - orderType={}, order_type={}, type={}", 
                orderType, order_type, type);
        // 兼容多种参数名
        String finalOrderType = StringUtils.isNotBlank(orderType) ? orderType :
                (StringUtils.isNotBlank(order_type) ? order_type :
                        (StringUtils.isNotBlank(type) ? type : ""));
        logger.info("【OrderController】最终使用的订单类型：'{}'", finalOrderType);
        return ResponseObj.success().put(orderService.orderRecognition(file, finalOrderType));
    }

    /**
     * 电子单识别
     */
    @RequestMapping(value = "/electronicOrderRecognition", method = RequestMethod.POST)
    @ApiOperation("电子单识别")
    public ResponseObj<CreateOrderRequest> electronicOrderRecognition(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "order_type", required = false) String order_type,
            @RequestParam(value = "type", required = false) String type) {
        // 调试日志：输出接收到的所有参数
        logger.info("【OrderController】electronicOrderRecognition接口接收参数 - orderType={}, order_type={}, type={}", 
                orderType, order_type, type);
        // 兼容多种参数名
        String finalOrderType = StringUtils.isNotBlank(orderType) ? orderType :
                (StringUtils.isNotBlank(order_type) ? order_type :
                        (StringUtils.isNotBlank(type) ? type : ""));
        logger.info("【OrderController】最终使用的订单类型：'{}'", finalOrderType);
        return ResponseObj.success().put(orderService.electronicOrderRecognition(file, finalOrderType));
    }

    /**
     * 同步用友
     */
    @RequestMapping(value = "/syncToYongyou", method = RequestMethod.POST)
    @ApiOperation("同步用友")
    public ResponseObj syncToYongyou(@RequestBody YongyouSyncRequest request) {
        orderService.syncToYongyou(request);
        return ResponseObj.success();
    }
    /**
     * 订单导出
     */
    @RequestMapping(value = "export/{id}", method = RequestMethod.GET)
    @ApiOperation("订单导出")
    public void export(HttpServletResponse response, @PathVariable Long id) throws IOException {
        orderService.export(response, id);
    }


}
