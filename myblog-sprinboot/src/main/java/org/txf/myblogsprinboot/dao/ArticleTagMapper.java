package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.txf.myblogsprinboot.model.ArticleTag;

import java.util.List;

@Mapper
public interface ArticleTagMapper {
    int batchInsertArticleTag(@Param("articleTagList")List<ArticleTag> list);
}
