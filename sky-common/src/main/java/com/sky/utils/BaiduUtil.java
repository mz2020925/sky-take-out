package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.BaiduProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Data
@Slf4j
public class BaiduUtil {
    @Autowired
    private BaiduProperties baiduProperties;


    public String getCoordinate(String address) throws Exception {

        Map params = new HashMap<>();  // HashMap()是无序的，但是LinkedHashMap是有序的
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", baiduProperties.getAk());
        // params.put("callback", "showLocation");

        log.info("调用百度API获取经纬度...");
        String response = HttpClientUtil.doGet(baiduProperties.getCoordinateUrl(), params);
        JSONObject jsonObject = JSON.parseObject(response);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException(jsonObject.getString("message"));
        }
        String lng = jsonObject.getJSONObject("result").getJSONObject("location").getString("lng");
        String lat = jsonObject.getJSONObject("result").getJSONObject("location").getString("lat");

        String latLng = lat + "," + lng;
        log.info("经纬度计算结果：{}", latLng);
        return latLng;
    }

    public double getDistance(String origin, String destination) throws Exception {
        Map params = new HashMap<>();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("output", "json");
        // params.put("riding_type", "1");  // 默认0：0-普通 1-电动车
        params.put("ak", baiduProperties.getAk());

        log.info("调用百度API计算距离...");
        String response = HttpClientUtil.doGet(baiduProperties.getDistanceUrl(), params);
        JSONObject jsonObject = JSON.parseObject(response);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException(jsonObject.getString("message"));
        }
        // JSONObject result = jsonObject.getJSONObject("result");
        // System.out.println(result.toString());
        // JSONArray routes = result.getJSONArray("routes");
        // System.out.println(routes);
        // double distance = routes.getJSONObject(0).getDoubleValue("distance");
        // System.out.println(distance);

        double distance = jsonObject.getJSONObject("result").getJSONArray("routes").getJSONObject(0).getDoubleValue("distance");
        log.info("距离计算结果：{}", distance);
        return distance;
    }
}