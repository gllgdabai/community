package com.dabai.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 激活类型：成功，失败
 * @author
 * @create 2022-03-29 17:38
 */
@Getter
@AllArgsConstructor
public enum ActivationEnum {
    /**
     * 激活成功
     */
    ACTIVATION_SUCCESS(0),
    /**
     * 重复激活
     */
    ACTIVATION_REPEAT(1),
    /**
     * 激活失败
     */
    ACTIVATION_FAILURE(2);

    private final Integer status;   //激活状态
}
