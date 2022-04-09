package com.dabai.community.service.impl;

import com.dabai.community.service.LikeService;
import com.dabai.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author
 * @create 2022-04-08 18:51
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 弃用：由于增加了统计用户收到的赞功能，点赞时需要同时更新实体和用户，要保证事务性
//    @Override
//    public void like(int userId, int entityType, int entityId) {
//
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        Boolean isLiked = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (isLiked) { // 该用户已点赞，再点击就是取消点赞
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        } else {
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
//    }


    @Override
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId); // 被点赞用户的key
                Boolean isLiked = redisTemplate.opsForSet().isMember(entityLikeKey, userId); //注意查询方法不要写在事务之中

                operations.multi(); // 开启事务

                if (isLiked) { // 该用户已点赞，再点击就是取消点赞
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();   // 提交事务
            }
        });
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

    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId); // 被点赞用户的key
        Integer likeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);

        return (likeCount== null) ? 0 : likeCount;
    }
}
