package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * Apache ECharts 是一款基于 Javascript 的数据可视化图表库，提供直观，生动，可交互，可个性化定制的数据可视化图表。
 * 官网地址：https://echarts.apache.org/zh/index.html
 */

/**
 * Apache POI 是一个处理Miscrosoft Office各种文件格式的开源项目。
 * 简单来说就是，我们可以使用 POI 在 Java 程序中对Miscrosoft Office各种文件进行读写操作。
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "统计报表相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;


    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("营业额统计");
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计接口")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("用户统计接口");
        UserReportVO userReportVO = reportService.userStatistics(begin, end);
        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计接口")
    public Result<OrderReportVO> ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("订单统计接口");
        OrderReportVO orderReportVO = reportService.ordersStatistics(begin, end);
        return Result.success(orderReportVO);
    }



    @GetMapping("/top10")
    @ApiOperation("查询销量排名top10接口")
    public Result<SalesTop10ReportVO> top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("查询销量排名top10接口");
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(begin, end);
        return Result.success(salesTop10ReportVO);
    }


    @GetMapping("/export")
    @ApiOperation("导出Excel报表接口")
    public void export(HttpServletResponse response)throws Exception{
        reportService.export(response);
    }



}
