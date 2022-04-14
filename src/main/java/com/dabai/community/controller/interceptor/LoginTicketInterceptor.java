package com.dabai.community.controller.interceptor;

import com.dabai.community.entity.LoginTicket;
import com.dabai.community.entity.User;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CookieUtil;
import com.dabai.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author
 * @create 2022-03-31 14:35
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取ticket
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            // 根据ticket查询登录凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 再根据登录凭证 查询 用户
            // 首先检查凭证的有效性(status,expired)
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                // 再本次请求中持有 用户
                hostHolder.setUser(user);

                // 构建用户认证的结果，并存入SecurityContext，以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    /*
    在模板引擎之前使用
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);   // 把user 传到模板引擎
        }
    }

    /*
    最后清除user数据
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
//        SecurityContextHolder.clearContext();
    }
}
