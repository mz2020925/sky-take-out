package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工管理相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录接口")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {  // 参数employeeLoginDTO是登录请求的请求体
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        // 登录成功后，生成jwt令牌。jwt令牌有什么用?JWT（JSON Web Token）是一种基于 JSON 格式的轻量级令牌（token）协议，它被广泛应用于网络应用程序的身份验证和授权。
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String admin_token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);  // jwt令牌是由三个信息编码而来的，存放到token变量中

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()  // 这种链式编程构建对象并属性赋值的写法，的前提是EmployeeLoginVO类上要加上注解@Builder
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(admin_token)
                .build();  // 把属性多的对象赋值给属性少的对象，不能用方法BeanUtils.copyProperties

        return Result.success(employeeLoginVO);  // 这也是链式编程属性赋值，这是登录请求的返回响应体
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出登录接口")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工功能-表现层方法
     * @param employeeDTO
     * @return
     */
    @PostMapping  // Restful风格前面已经有接口路径了，@RequestMapping("/admin/employee")
    @ApiOperation(value = "新增员工接口")
    public Result<String> save(@RequestBody EmployeeDTO employeeDTO){  // TODO 这个Result的泛型类的用法是什么。这里没有指定泛型的类型也可以吗
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> getByPage(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("分页查询：{}", employeePageQueryDTO);
        PageResult data = employeeService.getByPage(employeePageQueryDTO);  // DTO是用于前端传递给后端的数据的，所以是DTO
        return Result.success(data);  // 把业务逻辑层返回的数据封装到统一的Result中，然后再返回给前端
    }

    /**
     * 启用、禁用员工账号
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用员工账号")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        log.info("启用禁用：{}，员工账号：{}",status,id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工：{}", id);
        return Result.success(employeeService.getById(id));
    }

    /**
     * 根据id修改员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改员工信息")
    public Result<String> updateById(@RequestBody EmployeeDTO employeeDTO){
        log.info("根据id修改员工信息：{}", employeeDTO);
        employeeService.updateById(employeeDTO);
        return Result.success();
    }


    /**
     * 修改当前登录员工的密码
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改当前登录员工的密码")
    public Result<String> editPassword(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改当前登录员工的密码：{}", passwordEditDTO);
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}
