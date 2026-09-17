package org.txf.myblogsprinboot.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.txf.myblogsprinboot.advice.Result;
import org.txf.myblogsprinboot.utils.SessionUtils;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    ObjectMapper objectMapper;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 登录拦截
        log.info("进行登录拦截...");
        HttpSession session = request.getSession();
        // 登录失败的情况
        if (session == null || session.getAttribute(SessionUtils.USER_SESSION_KEY) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.nologin()));
            log.info("拦截：用户未登录");
            return false;
        }
        // 登录成功就放行，让controller处理接口
        log.info("登录拦截检查完毕，放行接口：" + request.getRequestURI());
        return true;
    }
}
