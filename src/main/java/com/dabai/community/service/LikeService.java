package com.dabai.community.service;

/** 点赞的业务层
 * @author
 * @create 2022-04-08 18:41
 */
public interface LikeService {
    /**
     *  点赞功能
     * @param userId 哪位用户点的赞
     * @param entityType 点赞对象的实体类型
     * @param entityId 点赞对象的实体Id
     */
    void like(int userId, int entityType, int entityId);

    /**
     *  查询某实体点赞的数量
     * @param entityType  实体类型
     * @param entityId 实体Id
     * @return
     */
    long findEntityLikeCount(int entityType, int entityId);

    /**
     *  查询 某用户 对 某实体 的点赞状态(0-未点赞，1-已点赞），以后业务扩展也可添加踩的功能
     * @param userId 用户Id
     * @param entityType 实体类型
     * @param entityId 实体Id
     * @return
     */
    int findEntityLikeStatus(int userId, int entityType, int entityId);
}
