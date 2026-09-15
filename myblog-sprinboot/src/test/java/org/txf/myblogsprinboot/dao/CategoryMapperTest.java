package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.vo.CategoryListVO;

@SpringBootTest
class CategoryMapperTest {
    @Autowired
    CategoryMapper categoryMapper;

    @Test
    void selectCategoryVO() {
        CategoryListVO categoryListVO = categoryMapper.selectCategoryVO(1L);
        System.out.println(categoryListVO);
    }
}