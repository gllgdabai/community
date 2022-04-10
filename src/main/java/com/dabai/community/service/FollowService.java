package com.dabai.community.service;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @create 2022-04-09 9:49
 */
public interface FollowService {
    /**
     *  关注
     * @param userId 当前用户Id
     * @param entityType 关注的实体类型
     * @param entityId 关注的实体Id
     */
    void follow(int userId, int entityType, int entityId);

    /**
     *  取消关注
     * @param userId 当前用户Id
     * @param entityType 关注的实体类型
     * @param entityId 关注的实体Id
     */
    void unfollow(int userId, int entityType, int entityId);

    /**
     *  查询当前用户关注的实体的数量
     * @param userId 当前用户的Id
     * @param entityType 关注的实体类型
     * @return 关注的数量
     */
    long findFolloweeCount(int userId, int entityType);

    /**
     *  查询该实体的粉丝数量
     * @param entityType 该实体的类型
     * @param entityId 该实体的Id
     * @return 粉丝的数量
     */
    long findFollowerCount(int entityType, int entityId);

    /**
     *  查询当前用户是否关注该实体
     * @param userId 当前用户的Id
     * @param entityType 该实体的类型
     * @param entityId 该实体的Id
     */
    boolean hasFollowed(int userId, int entityType, int entityId);

    /**
     *  (分页)查询某用户关注的人，(map中携带用户信息-"user"和关注时间-"followTime")
     * @param userId 该用户的Id
     * @param offset  分页的起始行号
     * @param limit   每页最多几条数据
     * @return 关注的人的相关信息列表
     */
    List<Map<String,Object>> findFollowees(int userId, int offset, int limit);

    /**
     *  (分页)查询某用户的粉丝列表，(map中携带用户信息-"user"和关注时间-"followTime")
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Map<String,Object>> findFollowers(int userId, int offset, int limit);
}
