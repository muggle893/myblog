package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.txf.myblogsprinboot.model.ArticleTag;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ArticleTagMapperTest {
    @Autowired
    ArticleTagMapper articleTagMapper;

    @Test
    @Transactional
    void batchInsertArticleTag() {
        // 准备数据
        List<ArticleTag> articleTags = new ArrayList<ArticleTag>();
        ArticleTag articleTag = new ArticleTag();
        articleTag.setArticleId(1L);
        articleTag.setTagId(2L);
        articleTag.setSortOrder(0);
        articleTags.add(articleTag);

        // 测试
        articleTagMapper.batchInsertArticleTag(articleTags);

    }
}