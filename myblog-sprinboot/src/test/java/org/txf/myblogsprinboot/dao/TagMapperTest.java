package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.vo.TagVO;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class TagMapperTest {

    @Autowired
    private TagMapper tagMapper;
    @Test
    void getTagsByIds() {
        List<Long> tagIds = new ArrayList<>();
        tagIds.add(1L);
        tagIds.add(16L);
        tagMapper.getTagsByIds(tagIds).forEach(System.out::println);
    }

    @Test
    void getTagsByArticleIds() {
        List<Long> articleIds = new ArrayList<>();
        articleIds.add(1L);
        articleIds.add(2L);
        tagMapper.getTagsByArticleIds(articleIds).forEach(System.out::println);
    }

    @Test
    void insertTag() {
        TagVO tagVO = new TagVO();
        tagVO.setName("test");
        int res = tagMapper.insertTag(tagVO);
        System.out.println(tagVO);
        System.out.println(res);
    }
}