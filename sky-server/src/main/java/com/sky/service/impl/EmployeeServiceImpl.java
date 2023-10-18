package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        // 对前端传过来的明文密码进行md5加密处理，然后在和数据库中的密码比对，数据库中存储的密码都是加密之后的
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
            // 这里创建了一个AccountLockedException对象(通过它的带参构造方法)，它的构造方法又调用父类BaseException的带参构造方法，
            // 创建了一个BaseException对象，BaseException也有继承的父类，
            // 也就是说这里抛出的异常对象有很多个，其中BaseException类型的异常会被hander包下的GlobalExceptionHandler类捕获
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工-业务逻辑层方法
     *
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        // 应该需要检查新增员工的账号是否已存在,——不需要，因为GlobalExceptionHandler中已经有SQL异常捕获方法来完成这个功能了，因为数据库中username设置的是唯一约束

        Employee employee = new Employee();
        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        // 设置该员工的登录状态
        employee.setStatus(StatusConstant.ENABLE);
        // 设置型新员工的密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        // 设置当前员工记录创建时间和修改时间
        /** 因为通过Spring AOP实现了公共字段统一赋值的，所以这4个属性不需要赋值了 **/
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());
        // 设置是谁（就是当前登录的管理员）创建和修改的该员工


        // employee.setCreateUser(BaseContext.getCurrentId());
        // employee.setUpdateUser(BaseContext.getCurrentId());  // 想要实现这个就需要了解前端和后端交互过程中的jwt认证相关知识，后端可以从前端穿过来的token中提取用户id
        // 调用持久层方法
        employeeMapper.insert(employee);
    }

    /**
     * 员工管理的分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult getByPage(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());  // 这行代码对后面有什么用呢？
        // MyBatis提供了一个插件pagehelper，这个插件底层是基于MyBatis的拦截器来编写的，说白了就是我们这个方法中用的mapper代理开发中的SQL语句都会被pagehelper处理，
        // 分页查询的SQL代码中没有加上 "limit 0,10" ，是因为上面那句代码就告诉了pagehelper插件，你后面在处理SQL代码的时候都追加上"limit getPage(),getPageSize()"

        Page page = employeeMapper.getByPage(employeePageQueryDTO);  // pagehelper这个插件要求查询的数据返回放在Page<>中，它继承了ArrayList
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
        employeeMapper.updateById(employee);
    }

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // employee.setPassword("******");
        return employee;
    }

    /**
     * 根据id修改员工信息
     * 这里和方法startOrStop中共用一套dao代码，即方法employeeMapper.update(employee)
     *
     * @param employeeDTO
     * @return
     */
    public void updateById(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 设置修改人(的id)和修改时间
        /** 因为通过Spring AOP实现了公共字段统一赋值的，所以这2个属性不需要赋值了 **/
        // employee.setUpdateUser(BaseContext.getCurrentId());
        // employee.setUpdateTime(LocalDateTime.now());

        employeeMapper.updateById(employee);
    }

    /**
     * 修改当前登录员工的密码
     *
     * @param passwordEditDTO
     * @return
     */
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long empId = passwordEditDTO.getEmpId();
        String oldPassword = passwordEditDTO.getOldPassword();
        String newPassword = passwordEditDTO.getNewPassword();

        oldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());  // 前端传递过来的旧密码编码之后的字符串
        String existOldPassword = getById(empId).getPassword();  // 数据库中已存在的旧密码编码之后的字符串

        // 旧密码对比
        if (!existOldPassword.equals(oldPassword)) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 如果旧密码一致，就修改密码
        Employee employee = new Employee();
        employee.setId(empId);
        employee.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        employeeMapper.updateById(employee);
    }
}

















