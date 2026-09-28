package org.txf.myblogsprinboot.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.txf.myblogsprinboot.utils.JwtsTokenUtils;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    ObjectMapper objectMapper;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 登录拦截
        log.info("进行登录拦截...");
        String userToken = request.getHeader("user-token");
        Claims claims = JwtsTokenUtils.parseJwtToken(userToken);
        if (claims == null) {
            log.info("登录拦截检查完毕，登录失败：claim为空.");
            return false;
        }
        // 登录成功就放行，让controller处理接口
        log.info("登录拦截检查完毕，放行接口：" + request.getRequestURI());
        return true;
    }
}
