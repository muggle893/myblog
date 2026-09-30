package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.txf.myblogsprinboot.model.Article;
import org.txf.myblogsprinboot.vo.ArticleListVO;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class ArticleMapperTest {
    @Autowired
    ArticleMapper articleMapper;

    @Test
    void selectAllArticle() {
        articleMapper.selectAllArticle("frank").forEach(System.out::println);

    }

    /**
     * 测试插入一条文章记录
     */
    @Test
    void insertArticle() {
        Article article = new Article();
        article.setTitle("测试插入文章记录");
        article.setContentMarkdown("# 测试插入文章记录");
        article.setAuthorId(1L);
        article.setPublishedAt(LocalDateTime.now());
        articleMapper.insertArticle(article);
    }

    @Test
    void selectArticleById() {
        Article article = articleMapper.selectArticleById(1L);
        System.out.println(article);
    }

    @Test
    void selectArticleByPage() {
        List<ArticleListVO> articleListVOS = articleMapper.selectArticleByPage(1, 1, 5);
        articleListVOS.forEach(System.out::println);
    }

    @Test
    void selectArticleCount() {
        int cnt = articleMapper.selectArticleCount(1);
        System.out.println(cnt);
    }

    @Test
    @Transactional
    void updateArticle() {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("测试");
        article.setContentMarkdown("测试");
        article.setRowVersion(0L);
        articleMapper.updateArticle(article);
    }

    @Test
    void selectArticleAuthorId() {
        int ret = (int) articleMapper.selectArticleAuthorId(1L);
        System.out.println(ret);
    }

    @Test
    @Transactional
    void deleteArticleById() {
        articleMapper.deleteArticleById(31L);
    }
}