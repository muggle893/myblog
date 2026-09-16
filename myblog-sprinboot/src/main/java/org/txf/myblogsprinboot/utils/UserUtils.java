package org.txf.myblogsprinboot.Utils;

import jakarta.servlet.http.HttpSession;
import org.txf.myblogsprinboot.enums.UserType;
import org.txf.myblogsprinboot.model.User;

/**
 * 关于用户的工具类
 */
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
        // 根据sessionkey拿到用户的登录状态
        User user = (User)session.getAttribute(SessionUtils.USER_SESSION_KEY);
        if (user == null) {
            // 未登录的情况
            return UserType.VISITOR_NOLOGIN;
        } else if (user.getUsername().equals(username)) {
            return UserType.VISITOR_LOGIN;
        } else {
            return UserType.VISITOR_NOLOGIN;
        }
    }
}
