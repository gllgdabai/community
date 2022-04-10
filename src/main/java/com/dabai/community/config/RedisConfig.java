package com.dabai.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 *  redis 配置类
 * @author
 * @create 2022-04-08 11:00
 */
@Configuration
public class RedisConfig {
    /**
     *  AutoWired首先根据 类型匹配 ,发现AutoConfig和自定义Config都是RedisTemplate类型
     *  所以只能再次根据 方法名匹配, AutoConfig源码里方法名为 redisTemplate
     *  如果自定义名也为redisTemplate就可以加载了，
     *  如果自定义不想自定义名为redisTemplate，就需要@Bean(name= 自定义)。
     */
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 需要设置序列化方式，否则传入redis的数据会乱码
        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}
