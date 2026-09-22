package org.txf.myblogsprinboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.txf.myblogsprinboot.utils.SessionUtils;
import org.txf.myblogsprinboot.advice.Result;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.service.UserService;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    // 12 是计算成本参数，可以根据服务器性能调整
    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(12);

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public Result login(String username, String password, HttpServletRequest request) {
        log.info("用户登录.");
        // 1.校验用户名和密码
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.info("用户登录：参数校验失败.");
            return Result.paramError("用户名或者密码不能为空！！！");
        }
        log.info("用户登录：校验参数成功.");

        // 2.调用service，查询用户
        User user = userService.getUserByUserName(username);
        if (user == null || user.getId() == null || user.getId() < 0) {
            log.info("用户登录：查不到此用户.");
            return Result.fail("用户登录失败，用户不存在.");
        }

        // 3.校验用户密码哈希值
        String storedPasswordHash = user.getPasswordHash();

        if (!StringUtils.hasText(storedPasswordHash)
                || !passwordEncoder.matches(password, storedPasswordHash)) {
            log.info("密码错误.");
            return Result.fail("用户名或者密码错误.");
        }

        // 4.将用户信息存到session中
        HttpSession session = request.getSession(true);
        // 登录后替换sessionId，防止黑客用旧的sessionId攻击
        request.changeSessionId();
        session.setAttribute(SessionUtils.USER_SESSION_KEY, user);
        log.info("用户登录成功.");

        return Result.success("用户登录成功.");
    }
}
