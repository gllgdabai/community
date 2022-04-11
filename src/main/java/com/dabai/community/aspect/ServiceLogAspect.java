package com.dabai.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  利用AOP实现统一记录日志
 * @author
 * @create 2022-04-07 16:46
 */
@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.dabai.community.service.impl.*.*(..))")
    public void pointcut() {}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户(ip) 在 xxx时间，访问了[com.dabai.community.service.xxx()]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return; //说明是一个特殊的调用，这里就不去处理了
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        log.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }

}
