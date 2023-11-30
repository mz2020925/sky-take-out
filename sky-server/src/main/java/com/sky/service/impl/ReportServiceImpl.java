package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;


    public List<LocalDate> getLocalDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates = new ArrayList<>();
        while (!begin.equals(end)) {
            localDates.add(begin);
            begin = begin.plusDays(1);  // 日期计算，获得指定日期后1天的日期
        }

        return localDates;
    }

    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 根据其实时间获取一天天的列表
        List<LocalDate> localDates = getLocalDateList(begin, end);

        // 创建响应体的data需要的列表
        List<Double> turnovers = new ArrayList<>();
        // 按照一天天的方式查询新增用户列表，和总用户列表
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin", beginTime);
            map.put("end", endTime);
            Double turnoverOfDay = orderMapper.turnover(map);  // java中的mybatis代理开发参数传递可以直接同名传递，也可以通过对象属性传递，也可以通过hashMap传递
            turnoverOfDay = turnoverOfDay == null ? 0.0 : turnoverOfDay;
            turnovers.add(turnoverOfDay);
        }
        // 数据封装，构建响应体中的data
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(localDates, ","))
                .turnoverList(StringUtils.join(turnovers, ","))
                .build();
    }

    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 根据其实时间获取一天天的列表
        List<LocalDate> localDates = getLocalDateList(begin, end);

        // 创建响应体的data需要的列表
        List<Double> newUserList = new ArrayList<>();
        List<Double> totalUserList = new ArrayList<>();
        // 按照一天天的方式查询新增用户列表，和总用户列表
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin", null);
            map.put("end", endTime);
            Double totalUse = orderMapper.userStatistics(map);
            totalUserList.add(totalUse);
            map.put("begin", beginTime);
            Double newUser = orderMapper.userStatistics(map);
            newUserList.add(newUser);
        }

        // 构建响应体中的data
        return UserReportVO.builder()
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        // 根据其实时间获取一天天的列表
        List<LocalDate> localDates = getLocalDateList(begin, end);

        // 创建响应体的data需要的列表
        List<Integer> orderCountList = new ArrayList<>();  // 订单数列表，以逗号分隔
        List<Integer> validOrderCountList = new ArrayList<>();  // 有效订单数列表，以逗号分隔
        // 按照一天天的方式查询新增用户列表，和总用户列表
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MIN);

            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", null);

            Integer orderCount = orderMapper.ordersStatistics(map);
            orderCountList.add(orderCount);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.ordersStatistics(map);
            validOrderCountList.add(validOrderCount);
        }

        Integer totalOrderCount = orderCountList.stream().reduce(0, (sub, item) -> sub + item);  // 订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(0, (sub, item) -> sub + item);  // 有效订单数
        Double orderCompletionRate = null;  // 订单完成率
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        // 构建响应体中的data
        return OrderReportVO.builder()
                .dateList(StringUtils.join(localDates, ","))
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(StringUtils.join(orderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .build();
    }
}
