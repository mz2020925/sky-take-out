package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

    @Autowired
    private UserMapper userMapper;


    public List<LocalDate> getLocalDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> localDates = new ArrayList<>();
        // 这个函数中需要把begin这一天和end这一天都包含进去
        localDates.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);  // 日期计算，获得指定日期后1天的日期
            localDates.add(begin);
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
            Double turnover = orderMapper.turnoverStatistics(map);  // java中的mybatis代理开发参数传递可以直接同名传递，也可以通过对象属性传递，也可以通过hashMap传递
            turnover = turnover == null ? 0.0 : turnover;
            turnovers.add(turnover);
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
            Double totalUse = userMapper.userStatistics(map);
            totalUserList.add(totalUse);
            map.put("begin", beginTime);
            Double newUser = userMapper.userStatistics(map);
            newUserList.add(newUser);
        }

        // 构建响应体中的data
        return UserReportVO.builder()
                .dateList(StringUtils.join(localDates, ","))
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
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

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
        Double orderCompletionRate = 0.0;  // 订单完成率
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


    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> top10Goods = orderMapper.top10(beginTime, endTime);

        String nameListString = top10Goods.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(","));
        String numberListString = top10Goods.stream().map(GoodsSalesDTO::getNumber).map(String::valueOf).collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameListString)
                .numberList(numberListString)
                .build();
    }

    public void export(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workSpaceService.businessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        try {
            // String filePath = System.getProperty("user.dir") + "\\sky-server\\src\\main\\resources\\template\\运营数据报表模板.xlsx";
            // String filePath = "E:\\coding_IntelliJIDEA\\sky-take-out\\sky-server\\src\\main\\resources\\template\\运营数据报表模板.xlsx";
            // FileInputStream in = new FileInputStream(new File(filePath));
            // InputStream in = this.getClass().getClassLoader().getResourceAsStream(filePath);
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            // 通过输入流读取指定的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            // 获取Excel文件的第1个Sheet页
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);

            XSSFRow row4 = sheet.getRow(3);  // 第4行（从0开始）
            row4.getCell(2).setCellValue(businessDataVO.getTurnover());  // 营业额
            row4.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());  // 订单完成率
            row4.getCell(6).setCellValue(businessDataVO.getNewUsers());  // 订单完成率
            XSSFRow row5 = sheet.getRow(4);
            row5.getCell(2).setCellValue(businessDataVO.getValidOrderCount());  // 有效订单数
            row5.getCell(4).setCellValue(businessDataVO.getUnitPrice());  // 平均客单价

            // 获取Sheet页中的最后一行的行号
            for (int i = 7; i <= 36; i++) {
                XSSFRow row = sheet.getRow(i);
                row.getCell(1).setCellValue(String.valueOf(begin));
                BusinessDataVO businessDataVO1 = workSpaceService.businessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(begin, LocalTime.MAX));
                row.getCell(2).setCellValue(businessDataVO1.getTurnover());
                row.getCell(3).setCellValue(businessDataVO1.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVO1.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataVO1.getUnitPrice());
                row.getCell(6).setCellValue(businessDataVO1.getNewUsers());
                begin = begin.plusDays(1);
            }

            // 通过输出流将文件下载到客户端浏览器中
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            // 关闭资源
            out.flush();
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
