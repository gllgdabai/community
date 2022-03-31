package com.dabai.community.dao;

import com.dabai.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @author
 * @create 2022-03-30 19:30
 */
@Mapper
public interface LoginTicketMapper {
    @Insert("insert into login_ticket(user_id, ticket, status, expired) " +
            "values (#{userId},#{ticket},#{status},#{expired})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired "+
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update(
            "update login_ticket set status=#{status} where ticket=#{ticket} "
    )
    int updateStatus(String ticket, int status);




}
