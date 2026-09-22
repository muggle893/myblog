package org.txf.myblogsprinboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.txf.myblogsprinboot.dao.CategoryMapper;
import org.txf.myblogsprinboot.vo.CategoryListVO;

import java.util.List;

/**
 * 这个类用来提供和分类相关的功能
 */

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<CategoryListVO> getAllCategory() {
        return categoryMapper.selectAllCategory();
    }
}
