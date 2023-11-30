package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.dto.OrdersCancelDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    /**
     * 根据订单号和用户id查询订单
     *
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void updateOrders(Orders orders);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Select("select COUNT(id) from orders where status= #{status}")
    Integer statistics(Integer status);

    @Select("select number from orders where id=#{id}")
    String getOutTradeNoById(Long id);


    Double turnover(Map map);
}
