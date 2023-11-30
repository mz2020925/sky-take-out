package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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

    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates = new ArrayList<>();
        while (!begin.equals(end)) {
            localDates.add(begin);
            begin = begin.plusDays(1);  // 日期计算，获得指定日期后1天的日期
        }

        List<Double> turnovers = new ArrayList<>();
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
        // 数据封装

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(localDates, ","))
                .turnoverList(StringUtils.join(turnovers, ","))
                .build();
    }
}
