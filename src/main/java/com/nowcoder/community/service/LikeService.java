package com.nowcoder.community.service;


import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    //点赞

    /**
     * @param userId       点赞的人的id
     * @param entityType
     * @param entityId
     * @param entityUserId 实体作者（发布帖子或者发布评论的人）
     */

    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//
//        Boolean ismember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (ismember) {
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                //发布实体的作者的收到的赞 key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //查询点赞的人之前有没有点过赞
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();

                //之前点过赞
                if (isMember) {
                    //再去点赞就是取消赞，对实体点赞取消，取消点赞的人的id
                    operations.opsForSet().remove(entityLikeKey, userId);
                    //发布实体的人的收到的赞-1
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    //之前没人点过赞，再去点赞,增加点赞的人的id
                    operations.opsForSet().add(entityLikeKey, userId);
                    //发布实体的人的收到的赞+1
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    //查询某实体点赞的数量，有多少个userId
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态 有没有点过赞
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }


    //查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId) {
        //发布实体的作者的收到的赞的key ，value是数字
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();

    }
}
