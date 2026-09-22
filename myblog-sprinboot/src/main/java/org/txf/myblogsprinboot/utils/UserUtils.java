package org.txf.myblogsprinboot.utils;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.txf.myblogsprinboot.enums.UserType;
import org.txf.myblogsprinboot.model.User;

/**
 * 关于用户的工具类
 */
@Slf4j
public class UserUtils {

    /**
     * 获取用户的类型
     * 1.未登录的游客
     * 2.登录了但是不是作者
     * 3.登录了同时也是作者
     * @param username  表示要访问哪个作者的文章列表
     * @param session   根据session和sessionkey用来判断用户是否登录
     * @return
     */
    public static UserType getUserType(String username, HttpSession session) {
        log.info("用户工具类getUserType方法判断用户类型.");
        // 根据sessionkey拿到用户的登录状态
        User user = (User)session.getAttribute(SessionUtils.USER_SESSION_KEY);
        if (user == null) {
            // 未登录的情况
            return UserType.VISITOR_NOLOGIN;
        } else if (user.getUsername().equals(username)) {
            return UserType.AUTHOR;
        } else {
            return UserType.VISITOR_LOGIN;
        }
    }
}
