package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工管理的员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 员工管理的新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 员工管理的分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult getByPage(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 根据id修改员工信息
     * @param employeeDTO
     * @return
     */
    void updateById(EmployeeDTO employeeDTO);

    /**
     * 修改当前登录员工的密码
     * @param passwordEditDTO
     * @return
     */
    void editPassword(PasswordEditDTO passwordEditDTO);
}
