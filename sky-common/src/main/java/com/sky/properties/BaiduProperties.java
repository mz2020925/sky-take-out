package com.sky.properties;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.baidu")  // 这个注解的作用是把Spring核心配置类application.yml中的sky:jwt:的信息封装到本类中。作用有点类似于Spring中的jdbc.properties
@Data
public class BaiduProperties {
    private String shopAddress;
    private String ak;
    private String coordinateUrl;
    private String distanceUrl;


}
