package com.dabai.community.utils;

/**
 * @author
 * @create 2022-04-08 18:34
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee";   // 关注者

    private static final String PREFIX_FOLLOWER = "follower";   // 粉丝

    private static final String PREFIX_KAPTCHA = "kaptcha";    // 验证码

    private static final String PREFIX_TICKET = "ticket";   // 登录凭证

    private static final String PREFIX_USER = "user";   // 缓存用户信息

    // 某个实体的赞, 格式为 like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户收到的赞，格式为 like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体，格式为 followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体的粉丝，格式为 follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码, 格式为 kaptcha:owner
    public static String getKaptchaKey(String owner) {  // owner是给客户端临时的凭证，一个随机生成的字符串
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证, 格式为 ticket:ticket
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户信息, 格式为 user:userId
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
