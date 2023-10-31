package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     * 注意：这个方法执行时间在请求到达Controller方法之前，也就是说所有“请求Controller”的请求都会被这个方法拦截。
     * 但是登录操作，访问登录的Controller方法并没有被拦截，为什么呢？
     * 在通过SWagger测试的时候，如果不先登录获取一个token（它是一个存储着jwt令牌的变量）然后把它放到请求头中，也是会响应401的
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // System.out.println("当前线程id："+Thread.currentThread().getId());  // 获取当前线程的id
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法,动态方法就是Controller方法，直接放行
            return true;
        }

        // TODO 这个jwt拦截器什么时候起作用？登录的时候请求头中并没有jwt令牌啊？
        // 因为在WebMvcConfiguration中自定义拦截器设置了 .excludePathPatterns("/admin/employee/login") ——将这个Conroller接口方法排除在外了
        // 登录Controller方法中存在创建令牌的代码，这个Controller是一开始创建令牌的，需要放行，不验证jwt令牌
        // 请求其他Controller的请求，都会在进入Controller方法之前，被拦截器拦截。
        // 1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 2、校验令牌
        try {
            log.info("用户端jwt校验: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);  // 有了getAdminSecretKey()密钥才能解析token（这个变量里面存放着jwt令牌）
            // claims是一个HashMap<String, Object>，存了一个键值对(JwtClaimsConstant.EMP_ID, employee.getId())
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            // log.info("当前用户{}", userId);
            BaseContext.setCurrentId(userId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            log.error("用户端jwt校验异常：{}", ex.toString());
            response.setStatus(401);
            return false;
        }
    }
}