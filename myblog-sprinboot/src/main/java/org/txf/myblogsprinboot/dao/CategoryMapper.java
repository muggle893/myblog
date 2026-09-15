package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.txf.myblogsprinboot.vo.CategoryListVO;

@Mapper
public interface CategoryMapper {
    CategoryListVO selectCategoryVO(@Param("id")Long id);
}
