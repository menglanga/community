package com.nowcoder.community.service;


import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 分页查询用户的帖子总数
     * @param userId userId=0，查询全部帖子
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost>  findDiscussPosts(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    /**
     * 根据用户ID查询帖子总数  userid=0  全部人发布帖子的数量
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }


    /**
     * 发布帖子
     * @param discussPost
     * @return
     */
    public int addDiscussPost(DiscussPost discussPost){
        if (discussPost==null){
            throw  new IllegalArgumentException("参数不为空!");
        }

        //转义HTML标记，也就是防止html脚本注入
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.InsertDiscussPost(discussPost);

    }

    /**
     * 根据帖子id查询帖子
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }



    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id, status);
    }

}
