package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // @Insert("insert into category " +
    //         "(type, name, sort, status, create_time, update_time, create_user, update_user)" +
    //         "values" +
    //         " (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    // void save(Category category);
    //
    // @Select("select * from category where name = #{name}")
    // Category getByName(String name);

    @AutoFill(value = OperationType.INSERT)
    default void insertCategory(Category category){
        this.insert(category);
    }

    @AutoFill(value = OperationType.UPDATE)
    default void updateCategory(Category category){
        this.updateById(category);
    }

    @Select("select count(id) from category where status=#{status}")
    Integer overviewSetmeals(Integer status);
}
