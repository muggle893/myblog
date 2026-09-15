package org.txf.myblogsprinboot.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.enums.UserType;

@SpringBootTest
class ArticleServiceTest {
    @Autowired
    ArticleService articleService;

    @Test
    void getArticleList() {
        articleService.getArticleList("frank", UserType.VISITOR_LOGIN).forEach(System.out::println);
    }
}