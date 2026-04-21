package com.bjdx.rice.business.service;

import com.bjdx.rice.business.dto.MyPage;
import com.bjdx.rice.business.dto.order.*;
import com.bjdx.rice.business.entity.Orders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author ymh
 * @date 2025/12/13 16:28
 */
public interface OrderService {
    Orders createOrder(CreateOrderRequest request);

    Orders updateOrder(UpdateOrderRequest request);

    void deleteOrder(Long id);

    OrderDetailResponse getOrderDetail(Long id);

    MyPage<OrderPageResponse> queryOrderList(OrderPageQueryRequest request);

    void finishOrder(Long id);

    CreateOrderRequest orderRecognition(MultipartFile file, String orderType);

    void export(HttpServletResponse response, Long id) throws IOException;

    void updateOrderStatus(UpdateOrderStatusRequest request);

    void syncToYongyou(Long id);

    void batchSync(List<Long> ids);

    CreateOrderRequest electronicOrderRecognition(MultipartFile file, String orderType);

    void syncToYongyou(YongyouSyncRequest request);
}
