package com.dabai.community.controller.advice;

import com.dabai.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/** 统一异常处理
 * @author
 * @create 2022-04-07 15:09
 */
/** ControllerAdvice注解修饰类，表示该类是Controller的全局配置类
 * 配置：ControllerAdvice这个注解只去扫描带有@Controller注解的bean */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    @Autowired
    private static final Logger log = LoggerFactory.getLogger(ExceptionAdvice.class);

    /** ExceptionHandler注解，修饰的方法会在Controller出现异常后被调用，用于处理捕获到的异常 */
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("服务器发送异常：" + e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            log.error(element.toString());
        }
        // 判断浏览器的请求类型：普通请求还是异步请求
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {  // 说明是异步请求
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJsonString(1,"服务器异常!"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
