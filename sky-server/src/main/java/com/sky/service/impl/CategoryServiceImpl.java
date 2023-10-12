package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import com.sky.exception.CategoryNameDuplicateException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public void save(CategoryDTO categoryDTO) {
        // 检查菜品分类的名字是否已存在
        // String name = categoryDTO.getName();
        // LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<Category>();
        // lqw.eq(Category::getName, name);
        // Category category = categoryMapper.selectOne(lqw);
        // if (category != null) {
        //// 一个菜品分类的名字重复异常，不用在这里解决这个问，GlobalExceptionHandler中已经有SQL异常捕获方法来完成这个功能了，因为数据库中name设置的是唯一约束
        //     throw new CategoryNameDuplicateException(MessageConstant.CATEGORY_NAME_DUPLICATE);
        // }

        // 如果不存在，就新增
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(StatusConstant.ENABLE);

        // category.setCreateTime(LocalDateTime.now());
        // category.setUpdateTime(LocalDateTime.now());
        // category.setCreateUser(BaseContext.getCurrentId());
        // category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }


    public void deleteById(Long id) {
        categoryMapper.deleteById(id);
    }


    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.updateById(category);
    }

    public void update(CategoryDTO categoryDTO) {
        // 构造categoryMapper.updateById()的参数
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);


        // category.setUpdateTime(LocalDateTime.now());
        // category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
    }


    public List getByType(Integer type) {
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<Category>();
        lqw.eq(Category::getType, type);
        return categoryMapper.selectList(lqw);
    }


    public PageResult getByPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 获取起始页码，每页页码数
        IPage<Category> page = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        // 获取name参数，type参数
        String name = categoryPageQueryDTO.getName();
        Integer type = categoryPageQueryDTO.getType();

        // 构建查询条件
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<Category>();
        lqw.like(null != name, Category::getName, name)
                .eq(null != type, Category::getType, type)
                .orderByDesc(Category::getUpdateTime);
        IPage<Category> iPage = categoryMapper.selectPage(page, lqw);

        // 返回PageResult对象
        return new PageResult(iPage.getTotal(), iPage.getRecords());
    }
}
