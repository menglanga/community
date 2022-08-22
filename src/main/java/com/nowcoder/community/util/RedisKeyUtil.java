package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPILT = ":";
    //被赞的实体（1-帖子，2-评论）
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    //被赞的人的key的前缀名
    private static final String PREFIX_USER_LIKE = "like:user";

    //被关注者
    private static final String PREFIX_FOLLOWEE = "followee";
    //关注者
    private static final String PREFIX_FOLLOWER = "follower";


    //存验证码的key的前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //登录凭证的前缀key
    private static final String PREFIX_TICKET = "ticket";
    //缓存用户的前缀key
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

    //某个实体的赞 value存用户ID
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPILT + entityType + SPILT + entityId;
    }

    //某个用户的赞,也就是被赞的人的key
    //like:user:userId  ->int

    /**
     * @param userId 被点赞的人 值是数字 某人拥有多少的赞
     * @return
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType  -> Zset(entityId,now)

    /**
     * @param userId     follower 关注者
     * @param entityType 被关注的实体也就是 followee 被关注者  存的是实体ID 和关注时的时间
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPILT + userId + SPILT + entityType;
    }

    //某个实体拥有的粉丝 存的值是用户ID 和关注时的时间
    //follower:entityType:entityId -> Zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPILT + entityType + SPILT + entityId;
    }

    //验证码完整的key
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPILT + owner;
    }

    //登录凭证完整的key
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPILT + ticket;
    }

    //缓存用户的key
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPILT + userId;
    }

    //单日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPILT + date;
    }

    //区间uV 从那天到那天
    public static String getUVkey(String startDate, String endDate) {
        return PREFIX_UV + SPILT + startDate + SPILT + endDate;
    }

    //单日活跃用户
    public static String getDAUkey(String date) {
        return PREFIX_DAU + SPILT + date;
    }

    //区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPILT + startDate + SPILT + endDate;
    }


}
