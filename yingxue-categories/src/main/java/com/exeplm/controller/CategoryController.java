package com.exeplm.controller;

import com.exeplm.dto.CategoryDto;
import com.exeplm.entity.Category;
import com.exeplm.modules.aop.OperationLogAnnotation;
import com.exeplm.service.CategoryService;
import com.exeplm.utils.TaoResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 分类(Category)表控制层
 *
 * @author makejava
 * @since 2022-07-10 20:02:36
 */
@RestController
public class CategoryController {
    /**
     * 服务对象
     */
    @Resource
    private CategoryService categoryService;
    /**
     * 分类列表
     */
    @OperationLogAnnotation(operModul = "分类模块",operType = "列表",operDesc = "打开分类列表")
    @GetMapping("/categories")
    public ResponseEntity<TaoResult> categories(){
        List<CategoryDto> categories = categoryService.getCategories();
        return new ResponseEntity<>(TaoResult.ok(categories), HttpStatus.OK);
    }
    /**
     * 根据id查询类别
     */
    @OperationLogAnnotation(operModul = "分类模块",operType = "分类",operDesc = "根据id查询分类")
    @GetMapping("/{id}")
    public ResponseEntity<TaoResult> getCategory(@PathVariable("id")Integer id){
        Category category = categoryService.queryById(id);
        return new ResponseEntity<>(TaoResult.ok(category),HttpStatus.OK);
    }
}

