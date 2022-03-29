package com.dabai.community.entity;

import lombok.Data;

/**
 *  封装分页相关的信息
 * @author
 * @create 2022-03-28 18:48
 */
@Data
public class Page {

    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数
    private int rows;
    // 查询路径(用于复用分页链接)
    private String path;

    // 获取当前页的起始行
    public int getOffset() {
        // current * limit - limit
        return (current - 1) * limit;
    }

    // 获取总页数
    public int getTotal() {
        return (rows % limit == 0) ? rows/limit : rows/limit + 1;
    }

    // 获取起始页码
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    // 获取结束页码
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }

}
