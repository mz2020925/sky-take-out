package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * 只需要用到数据库表的账号列，所以只需要一个变量即可
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * 需要用到数据库表的所有列，所以要用entity对象
     * @param employee
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into employee " +
            "(name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);

    /**
     * 分页查询
     * 此处使用 mybatis 的分页插件 PageHelper 来简化分页代码的开发。底层基于 mybatis 的拦截器实现。
     * 需要用到数据库表的name（昵称）列，但是前端传给后端请求的时候，还需要传递第几页开始，一页有几条数据
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号，其实就是根据id修改员工的status属性
     * @param employee
     */
    @AutoFill(value=OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Select("select * from employee where id=#{id}")
    Employee getById(Long id);
}
