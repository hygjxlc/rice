package com.bjdx.rice.business.mapper;




import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;
import com.bjdx.rice.business.entity.OrderItems;

import java.util.List;

/**
 * @Author makejava  
 * @Desc 订单商品明细表(OrderItems)表数据库访问层
 * @Date 2025-12-13 16:20:20
 */
@Repository
public interface OrderItemsMapper extends Mapper<OrderItems>,MySqlMapper<OrderItems> {

    void deleteByOrderId(Long orderId);

    List<OrderItems> selectByOrderId(Long orderId);
}
