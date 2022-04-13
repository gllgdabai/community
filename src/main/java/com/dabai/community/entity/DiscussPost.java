package com.dabai.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;

/** 讨论贴子 类
 * @author
 * @create 2022-03-28 15:12
 */
@Data
//es 7.0需注意地方 shards ,replicas 在document弃用，改用setting注入
@Setting(shards = 6,replicas = 3)
@Document(indexName = "discusspost")
public class DiscussPost {
    @Id
    private int id;

    @Field(type = FieldType.Integer)
    private int userId;
    // eg: 互联网校招 --> 互联网 互联 联网 网校 校招(ik_max_word) --> 互联网 校招(ik_smart)
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private int type;   // 0-普通; 1-置顶;

    @Field(type = FieldType.Integer)
    private int status; // 0-正常; 1-精华; 2-拉黑;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Integer)
    private int commentCount;

    @Field(type = FieldType.Double)
    private double score;
}
