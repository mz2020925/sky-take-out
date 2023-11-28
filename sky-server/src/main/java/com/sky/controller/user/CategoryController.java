package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "C端-分类接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据分类类型查询分类
     * 这个用户端接口其实不会传递参数过来，也就是说type=null，这个时候就是查询所有分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类类型查询分类")
    @Cacheable(cacheNames = "categoryCache", key = "'categoryType_'+#type")
    public Result<List> getByType(Integer type) {
        log.info("C端-根据分类类型查询分类：{}", type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }
}
