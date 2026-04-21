package com.bjdx.rice.business.mapper;


import com.bjdx.rice.business.dto.order.OrderPageQueryRequest;
import com.bjdx.rice.business.dto.order.OrderPageResponse;
import com.bjdx.rice.business.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

/**
 * @Author makejava  
 * @Desc 订单商品明细表(OrderItems)表数据库访问层
 * @Date 2025-12-13 16:20:20
 */
@Repository
public interface OrdersMapper extends Mapper<Orders>,MySqlMapper<Orders> {

    List<OrderPageResponse> queryOrderList(@Param("request") OrderPageQueryRequest request);

    void finishOrder(Long id);

    void updateOrderStatus(@Param("id") Long id, @Param("status") String status);
}
