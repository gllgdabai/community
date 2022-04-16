package com.dabai.community.controller.interceptor;

import com.dabai.community.entity.User;
import com.dabai.community.service.DataService;
import com.dabai.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 做数据统计的拦截器
 * @author
 * @create 2022-04-15 10:13
 */
@Component
public class DateInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dateService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost();
        dateService.recordUV(ip);

        // 统计DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dateService.recordDAU(user.getId());
        }

        return true;    // 统计后，请求继续执行
    }
}
