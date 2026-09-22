package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.model.Article;

import java.time.LocalDateTime;

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

}