package com.dabai.community.service;

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
     * @return
     */
    boolean hasFollowed(int userId, int entityType, int entityId);

}
