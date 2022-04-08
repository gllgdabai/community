package com.dabai.community.utils;

/**
 * @author
 * @create 2022-04-08 18:34
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 某个实体的赞, 格式为 like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}
