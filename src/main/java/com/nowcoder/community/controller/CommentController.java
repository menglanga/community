package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    // TODO 不登录状态下是无法评论的 待完善
    @LoginRequired
    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(holder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(holder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        //给帖子的评论
        if (comment.getEntityType() == ENTITY_TYPE_POST) {

            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());

            event.setEntityUserId(target.getUserId());
        //给评论的评论（即回复别人的评论）
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {

            Comment target = commentService.findCommentById(comment.getId());

            event.setEntityUserId(target.getUserId());
        }


        eventProducer.fireEvent(event);
        //因为对帖子的评论，帖子实体中评论数量会增加，相当于修改了帖子实体中属性，要重新覆盖ES中帖子，所以要重新给ES发帖子
        if (comment.getEntityType()==ENTITY_TYPE_POST){
            //触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(holder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }


        return "redirect:/discuss/details/" + discussPostId;

    }

}
