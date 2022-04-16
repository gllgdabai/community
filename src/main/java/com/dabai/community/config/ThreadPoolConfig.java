package com.dabai.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** spring线程池配置类
 * @author
 * @create 2022-04-15 18:30
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

}
