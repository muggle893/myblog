package org.txf.myblogsprinboot.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.vo.TagVO;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class TagServiceTest {

    @Autowired
    private TagService tagService;
    @Test
    void getAuthorTags() {
        User user = new User();
        user.setId(1L);
        user.setUsername("frank");
        List<TagVO> authorTags = tagService.getAuthorTags(user);
        System.out.println(Arrays.toString(authorTags.toArray()));
    }
}