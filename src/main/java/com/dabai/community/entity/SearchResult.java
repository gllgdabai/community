package com.dabai.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 自定义实体
 *  用于暂存es中查询到的列表和总行数
 * @author
 * @create 2022-04-13 16:38
 */
@Data
@AllArgsConstructor
public class SearchResult<T> {
    private List<T> list;
    private long total;
}
