package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //分页查询
    List<DiscussPost>  selectDiscussPosts(int userId, int offset, int limit);

    //@Param 用于给参数起别名     如果只有一个参数，并且在<if>里使用，必须使用注解标注别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int InsertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);
}
