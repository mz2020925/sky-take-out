package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    // TODO 我记得Spring中讲过创建Bean，分为创建一个Bean，和创建不同的Bean。复习一下！！！
    @Autowired
    private DishMapper dishMapper;

    public void save(CategoryDTO categoryDTO) {
        // 不用检查菜品分类的名字是否已存在，因为GlobalExceptionHandler中已经有SQL异常捕获方法来完成这个功能了，因为数据库中name设置的是唯一约束

        // 新增
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(StatusConstant.ENABLE);

        // category.setCreateTime(LocalDateTime.now());
        // category.setUpdateTime(LocalDateTime.now());
        // category.setCreateUser(BaseContext.getCurrentId());
        // category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insertCategory(category);
    }


    public void deleteById(Long id) {
        // TODO 这个地方，因为分类表被套餐表和菜品表外键指向，所以不能直接删除的
        LambdaQueryWrapper<Setmeal> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(Setmeal::getCategoryId, id);
        List<Setmeal> setmeals = setmealMapper.selectList(lqw1);
        if (setmeals != null && setmeals.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        LambdaQueryWrapper<Dish> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Dish::getCategoryId, id);
        List<Dish> dishes = dishMapper.selectList(lqw2);
        if (dishes != null && dishes.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        categoryMapper.deleteById(id);
    }


    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.updateCategory(category);
    }

    public void update(CategoryDTO categoryDTO) {
        // 构造categoryMapper.updateById()的参数
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);


        // category.setUpdateTime(LocalDateTime.now());
        // category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateCategory(category);
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
