package com.dabai.community.service.impl;

import com.dabai.community.service.LikeService;
import com.dabai.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author
 * @create 2022-04-08 18:51
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void like(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember) { // 该用户已点赞，再点击就是取消点赞
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
