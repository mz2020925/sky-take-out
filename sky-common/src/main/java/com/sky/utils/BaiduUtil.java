package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.BaiduProperties;

import java.util.HashMap;
import java.util.Map;

public class BaiduUtil {

    public static String COORDINATE_URL = BaiduProperties.coordinateUrl;
    public static String DISTANCE_URL = BaiduProperties.distanceUrl;
    public static String AK = BaiduProperties.ak;


    public static String getCoordinate(String address) throws Exception {

        Map params = new HashMap<>();  // HashMap()是无序的，但是LinkedHashMap是有序的
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", AK);
        params.put("callback", "showLocation");

        String response = HttpClientUtil.doGet(COORDINATE_URL, params);
        JSONObject jsonObject = JSON.parseObject(response);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_PARSE_ERROR);
        }
        String lng = jsonObject.getJSONObject("result").getJSONObject("location").getString("lng");
        String lat = jsonObject.getJSONObject("result").getJSONObject("location").getString("lat");

        return lng + "," + lat;
    }

    public static Double getDistance(String origin, String destination){
        Map params = new HashMap<>();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("output", "json");
        params.put("riding_type", 1);  // 默认0：0-普通 1-电动车
        params.put("ak", AK);


        String response = HttpClientUtil.doGet(DISTANCE_URL, params);
        JSONObject jsonObject = JSON.parseObject(response);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_PARSE_ERROR);
        }

        return jsonObject.getJSONObject("result").getDoubleValue("distance");
    }



}