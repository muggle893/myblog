package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}