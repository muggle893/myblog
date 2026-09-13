package org.txf.myblogsprinboot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class UserControllerTest {


    @Test
    void login() {
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);
        // 这里只是演示密码
        String encodedPassword = encoder.encode("123456");

        // 仅用于本地初始化账号，把完整输出写入 password_hash 字段
        System.out.println(encodedPassword);
    }
}