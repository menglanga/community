package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    /**
     *
     * @param userId userId=0，查询全部帖子
     * @param offset
     * @param limit
     * @return
     */
    //分页查询
    //多个参数，使用if动态sql不需要起别名
    List<DiscussPost>  selectDiscussPosts(int userId, int offset, int limit);

    /**
     * 查询某人发布的帖子 userid=0  全部人发布帖子的数量
     * @param userId
     * @return
     */
    //@Param 用于给参数起别名     如果只有一个参数，并且在<if>里使用，必须使用注解标注别名
    //动态 SQL ，如果在动态 SQL 中使用了参数作为变量，那么也需要 @Param 注解，即使你只有一个参数
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 发布一个帖子
     * @param discussPost
     * @return
     */
    int InsertDiscussPost(DiscussPost discussPost);


    /**
     * 根据帖子id查询帖子,查询帖子详情
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id,int status);
}
