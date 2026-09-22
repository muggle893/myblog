package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.txf.myblogsprinboot.model.Article;
import org.txf.myblogsprinboot.vo.ArticleListVO;

import java.util.List;

@Mapper
public interface ArticleMapper {
    List<ArticleListVO> selectAllArticle(String author);

    int insertArticle(Article article);


}
