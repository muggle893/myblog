package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.model.User;

@SpringBootTest
class UserMapperTest {
    @Autowired
    UserMapper userMapper;

    @Test
    void selectUserById() {
        userMapper.selectUserByUsername("frank");
    }

    @Test
    void selectUserByUserId() {
        User user = userMapper.selectUserByUserId(1L);
        System.out.println(user);
    }
}