package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    void save(CategoryDTO categoryDTO);

    void deleteById(Long id);

    void startOrStop(Integer status, Long id);

    void update(CategoryDTO categoryDTO);

    List getByType(Integer type);

    PageResult getByPage(CategoryPageQueryDTO categoryPageQueryDTO);
}