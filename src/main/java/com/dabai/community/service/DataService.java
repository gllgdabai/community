package com.dabai.community.service;

import java.util.Date;

/** 数据统计
 * @author
 * @create 2022-04-15 9:44
 */
public interface DataService {
    /**
     *  将指定的Ip计入UV
     * @param ip 访客ip
     */
    void recordUV(String ip);

    /**
     *  统计指定日期范围内的UV
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计的结果
     */
    long calculateUV(Date startDate, Date endDate);

    /**
     *  将指定用户计入DAU
     * @param userId 指定用户Id
     */
    void recordDAU(int userId);

    /**
     *  统计指定日期范围内的DAU
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计的结果
     */
    long calculateDAU(Date startDate, Date endDate);
}
