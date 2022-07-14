package com.exeplm.handler;

import com.exeplm.annotation.RequiredToken;
import com.exeplm.entity.User;
import com.exeplm.exception.UserException;
import com.exeplm.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 自定义拦截器
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 进入方法前拦截处理
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取当前请求方法上是否存在RequiredToken注解
        if (((HandlerMethod)handler).getMethod().isAnnotationPresent(RequiredToken.class)){
            //有注解，进行增强

            String token = request.getParameter("token");
            if (!redisUtil.hasKey("session_"+token)){
                throw  new UserException("token已过期！");
            }
            User user = (User) redisUtil.get("session_" + token);
            request.setAttribute("user",user);
            request.setAttribute("token",token);
            //重置过期时间
            redisUtil.set("session_"+token,user,30, TimeUnit.MINUTES);
        }
        //放行
        return true;
    }
}
