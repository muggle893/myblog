package org.txf.myblogsprinboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.txf.myblogsprinboot.advice.Result;
import org.txf.myblogsprinboot.service.CategoryService;
import org.txf.myblogsprinboot.vo.CategoryListVO;

import java.util.List;

/**
 * 这个控制器类处理和分类相关的接口
 */

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有的分类
     * @return
     */
    @RequestMapping("/list")
    public Result<List<CategoryListVO>> getAllCategory() {
        List<CategoryListVO> list = categoryService.getAllCategory();
        return Result.success("获取分类列表成功.", list);
    }
}
