package com.dabai.community.event;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/** 封装事件对象
 * @author
 * @create 2022-04-11 15:30
 */
@Getter
public class Event {

    private String topic;   // 事件的主题
    private int userId; // 触发事件的用户的Id
    private int entityType; // 触发事件的实体的类型
    private int entityId; // 触发事件的实体的Id
    private int entityUserId;
    // 额外的数据存放与map中，保证可扩展性
    private Map<String, Object> data = new HashMap<>();

    /** Setter方法，支持链式编程 */
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
