package com.sky.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import com.sky.properties.MinIoProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
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

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private MinIoProperties minIoProperties;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        // 添加了两个拦截器，这两个拦截器会拦截不同请求url的请求
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");  //
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login")  // 不拦截这个Controller方法
                .excludePathPatterns("/user/shop/status");  // 不拦截这个Controller方法,为什么admin管理端不排除这个Controller方法，因为用户端是小程序，在登录之前就应该展示出店铺的登录状态。
    }

    /**
     * 通过knife4j生成接口文档
     * 下面是在本配置类中加入knife4j相关配置
     * 这个就是使用Swagger需要的配置类
     *
     * @return
     */
    @Bean
    public Docket docket1() {
        log.info("开始生成Swagger管理端接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo)
                .select()
                //指定生成接口需要扫描的包
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    @Bean
    public Docket docket2() {
        log.info("开始生成Swagger用户端接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(apiInfo)
                .select()
                //指定生成接口需要扫描的包
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * 这些静态资源就是后端自己测试访问到的接口文档页面
     *
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
     * 由于分页查询中的时间格式不正确，所以有两种解决方案，这里是第二种
     * 在WebMvcConfiguration中扩展 SpringMVC的消息转换器，实现统一对日期类型进行格式化处理。
     * 其实就是我们现在使用的是Spring MVC框架，这个框架本身有一个消息转换器，下面我们要扩展这个消息转换器，从而使它能够将返回的响应消息中的时间格式转换正确。
     * 什么是转换正确，就是后端返回给前端的json文本中时间格式应该是
     * "createTime": [
     * 2023-09-27 11:40:27
     * ],
     * 而不是
     * "createTime": [
     * 2023,
     * 9,
     * 26,
     * 15,
     * 9,
     * 30
     * ],
     * 第一种方式，比如是在类OrdersPageQueryDTO的时间属性"private LocalDateTime beginTime;"上面加上注解：
     * @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     * @param converters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始扩展SpringMVC消息转换器，修正分页查询中的时间格式...");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，可以将java对象转换为json字符串
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将我们自己的转换器放入spring MVC框架的容器中
        converters.add(0, converter);
    }

    /**
     * 解决druid 日志报错：discard long time none received connection:xxx
     * 另一种解决方法是在-VM options 里面加上 -Ddruid.mysql.usePingMethod=false
     */
    // @PostConstruct
    // public void setProperties() {
    //     System.setProperty("druid.mysql.usePingMethod", "false");
    // }

    /**
     * MyBatis-Plus要求你添加一个分页拦截器（PaginationInterceptor），这是因为分页查询涉及到对SQL语句的修改和重写，以实现正确的分页效果。
     * 分页拦截器是MyBatis-Plus提供的一个组件，它会拦截执行的SQL语句，并根据指定的分页参数，修改SQL语句以获取指定范围的数据。
     * 这个拦截器的作用和MyBatis的PageHelper插件一样。
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("开始创建MyBatis-Plus分页拦截器...");
        //1 创建MybatisPlusInterceptor拦截器对象
        MybatisPlusInterceptor mpInterceptor = new MybatisPlusInterceptor();
        //2 添加分页拦截器
        mpInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mpInterceptor;
    }

    /**
     * 本项目中使用一项技术 MinIO ，来搭建 存储服务器
     * 本函数返回 存储服务器 的一个Client连接。就类似于建立 MySQL 的一个连接一样。
     *
     * @return
     */
    @Bean
    public MinioClient minioClient() {
        log.info("开始创建minio存储服务器的一个Client连接...");
        return MinioClient.builder()
                .endpoint(minIoProperties.getEndpoint())
                .credentials(minIoProperties.getAccessKey(), minIoProperties.getSecretKey())
                .build();
    }

    /**
     * 本项目中使用到redis，通过Spring Data Redis来操作redis。
     * 下面创建RedisTemplate对象，就是在使用Spring Data Redis
     * 当前配置类不是必须的，因为 Spring Boot 框架会自动装配 RedisTemplate 对象，
     * 但是默认的key序列化器为JdkSerializationRedisSerializer，导致我们存到Redis中后的数据和原始数据有差别，
     * 故设置为StringRedisSerializer序列化器。
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 这里redisConnectionFactory不用管，因为引入的spring-boot-starter-data-redis依赖会创建一个redisConnectionFactory
        // 的Bean放入IOC容器中，我们不需要通过Autowired来注入
        log.info("开始创建Spring Data Redis模板对象...");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }


}
