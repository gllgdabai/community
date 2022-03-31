package com.dabai.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 登录凭证过期时间：默认状态，记住状态
 * @author
 * @create 2022-03-31 9:52
 */
@Getter
@AllArgsConstructor
public enum ExpirationEnum {
    /**
     *  默认状态的登陆凭证的过期时间，12小时
     */
    DEFAULT_EXPIRED_SECONDS(3600 * 12),
    /**
     * 记住状态的登陆凭证的过期时间，一周
     */
    REMEMBER_EXPIRED_SECONDS(3600 * 24 * 7);

    private final Integer expiration;   //过期时间
}
