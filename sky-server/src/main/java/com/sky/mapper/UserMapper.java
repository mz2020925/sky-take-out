package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select count(id) from user where create_time >= #{begin} and create_time <= #{end}")
    Integer newUsers(LocalDateTime begin, LocalDateTime end);

    Double userStatistics(Map map);
}
