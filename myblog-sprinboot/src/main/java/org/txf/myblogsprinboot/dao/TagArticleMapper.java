package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 这个Mapper用来处理Tag和Article关联表中的一些操作
 * 比如根据文章的id查找tag集合
 * 或者根据文章的tag查找文章的id集合
 */
@Mapper
public interface TagArticleMapper {
    List<Long> getTagIdListByArticleId(@Param("articleId")long articleId);
}
