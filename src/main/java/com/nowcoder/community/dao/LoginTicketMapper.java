package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;


@Mapper
//不推荐使用，改用缓存
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into Login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket ticket);

    @Select({
            "select id, user_id, ticket, status, expired ",
            "from Login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectLoginTicket(String ticket);

    @Update({
            "update login_ticket set status=#{status} where ticket=#{ticket}"
    })
    int updateStatus(String ticket,int status);

}
