package org.txf.myblogsprinboot.utils;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
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
     * @param request 根据请求头中的user-token来判断用户的类型
     * @return
     */
    public static UserType getUserType(String username, HttpServletRequest request) {
        log.info("用户工具类getUserType方法判断用户类型.");
        // 根据sessionkey拿到用户的登录状态
        String token = request.getHeader("user-token");
        JwtParser jwtParser = Jwts.parser().setSigningKey(JwtsTokenUtils.KEY);
        Claims claims = null;
        try {
            claims = jwtParser.parseClaimsJws(token).getBody();
        } catch(Exception e) {
            return UserType.VISITOR_NOLOGIN;
        }
        User user = new User();
        user.setUsername(claims.get("username", String.class));
        if (user.getUsername().equals(username)) {
            return UserType.AUTHOR;
        } else {
            return UserType.VISITOR_LOGIN;
        }
    }

    public static User getUserFromToken(HttpServletRequest request) {
        String token = request.getHeader("user-token");
        Claims claims = JwtsTokenUtils.parseJwtToken(token);
        User user = new User();
        user.setUsername(claims.get("username", String.class));
        user.setId(claims.get("id", Long.class));
        return user;
    }
}
