package com.dabai.community.service.impl;

import com.dabai.community.service.DataService;
import com.dabai.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @create 2022-04-15 9:45
 */
@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");


    @Override
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    @Override
    public long calculateUV(Date startDate, Date endDate) {
        // 检验数据合法性
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("请输入正确的时间段!");
        }

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1); // 加一天
        }

        // 合并这些数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(startDate), df.format(endDate));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());
        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    @Override
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    @Override
    public long calculateDAU(Date startDate, Date endDate) {
        // 检验数据合法性
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("请输入正确的时间段!");
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            keyList.toArray(new byte[0][0]);
            calendar.add(Calendar.DATE, 1); // 加一天
        }

        // 进行OR运算
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(startDate), df.format(endDate));
                connection.bitOp(
                    RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),
                        keyList.toArray(new byte[0][0])
                );
                return connection.bitCount(redisKey.getBytes());
            }
        });
        return (long) obj;
    }
}
