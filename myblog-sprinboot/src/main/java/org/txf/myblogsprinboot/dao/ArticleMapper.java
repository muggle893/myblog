package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.txf.myblogsprinboot.model.Article;
import org.txf.myblogsprinboot.vo.ArticleListVO;

import java.util.List;

@Mapper
public interface ArticleMapper {
    int deleteArticleById(@Param("id")long articleId);

    long selectArticleAuthorId(@Param("id")long articleId);

    List<ArticleListVO> selectAllArticle(String author);

    Article selectArticleById(Long id);

    int insertArticle(Article article);

    List<ArticleListVO> selectArticleByPage(@Param("authorId")long authorId, @Param("offset") int offset, @Param("pageSize")int pageSize);

    int selectArticleCount(@Param("authorId")long authorId);

    int updateArticle(Article article);
}
