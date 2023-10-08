package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");
    }

    /**
     * 通过knife4j生成接口文档
     * 下面是在本配置类中加入knife4j相关配置
     * @return
     */
    @Bean
    public Docket docket() {
        log.info("准备生成接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * 这些静态资源就是后端自己测试访问到的接口文档页面
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        // 后端接口自己测试访问Swagger http://localhost:8080/doc.html
        // Swagger会自动的把接口文档页面资源放到"classpath:/META-INF/resources/" 和 "classpath:/META-INF/resources/webjars/"下面
    }

    /**
     * 扩展MVC框架的消息转换器：
     * 由于分页查询中的时间格式不正确，所以有两种解决方案，这里是第二种，
     * 在WebMvcConfiguration中扩展 SpringMVC的消息转换器，实现统一对日期类型进行格式化处理。
     * 其实就是我们现在使用的是Spring MVC框架，这个框架本身有一个消息转换器，下载我们要扩展这个消息转换器，从而使它能够将返回的响应消息中的时间格式转换正确。
     * 什么是转换正确，就是后端返回给前端的json文本中时间格式应该是
     * "createTime": [
     *      2023-09-27 11:40:27
     * ],
     * 而不是
     * "createTime": [
     *      2023,
     *      9,
     *      26,
     *      15,
     *      9,
     *      30
     * ],
     * @param converters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters){
        log.info("开始扩展消息转换器");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，可以将java对象转换为json字符串
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将我们自己的转换器放入spring MVC框架的容器中
        converters.add(0, converter);
    }
}
