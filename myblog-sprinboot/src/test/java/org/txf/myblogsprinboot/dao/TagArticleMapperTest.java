package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TagArticleMapperTest {

    @Autowired
    private TagArticleMapper tagArticleMapper;
    @Test
    void getTagIdListByArticleId() {
        tagArticleMapper.getTagIdListByArticleId(1).forEach(System.out::println);
    }
}