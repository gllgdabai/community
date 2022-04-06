package com.dabai.community.dao;

import com.dabai.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @author
 * @create 2022-03-30 19:30
 */
@Mapper
public interface LoginTicketMapper {
    /**
     * 新增登录凭证
     * @param loginTicket 登陆凭证
     * @return 影响的行数
     */
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据ticket查询登录凭证
     * @param ticket Cookie中存放的ticket
     * @return  登录凭证
     */
    LoginTicket selectByTicket(String ticket);

    /**
     * 更新登陆凭证
     * @param ticket Cookie中存放的ticket
     * @param status 凭证状态，0-有效; 1-无效
     * @return 影响的行数
     */
    int updateStatus(String ticket, int status);

}
