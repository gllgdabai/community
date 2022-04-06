package com.dabai.community.common;

/** 维护项目中需要用到的一些常量
 * @author
 * @create 2022-04-04 21:29
 */
public class Constants {

    /** 激活成功 */
    public static final int ACTIVATION_SUCCESS = 0;

    /** 重复激活 */
    public static final int ACTIVATION_REPEAT = 1;

    /** 激活失败 */
    public static final int ACTIVATION_FAILURE = 2;

    /** 默认状态的登录凭证的超时时间 */
    public static final int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /** 记住状态的登录凭证超时时间 */
    public static final int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /** 实体类型: 帖子 */
    public static final int ENTITY_TYPE_POST = 1;

    /** 实体类型: 评论 */
    public static final int ENTITY_TYPE_COMMENT = 2;

    /** 实体类型: 用户 */
    public static final int ENTITY_TYPE_USER = 3;


}
