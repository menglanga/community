package com.nowcoder.community;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {


    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString() {
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }


    @Test
    public void testHash() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "leihao");
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testList() {
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().size(redisKey));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().size(redisKey));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().size(redisKey));


    }


    @Test
    public void testSet() {
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    @Test
    public void testSortedSet() {
        String redisKey = "test:students";
        redisTemplate.opsForZSet().add(redisKey, "汤神", 12);
        redisTemplate.opsForZSet().add(redisKey, "leihao", 124);
        redisTemplate.opsForZSet().add(redisKey, "nixi", 15);
        redisTemplate.opsForZSet().add(redisKey, "汤第三方", 95);
        redisTemplate.opsForZSet().add(redisKey, "是的", 125);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "leihao"));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey, "是的"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "是的"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));


    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    //多次访问同一key
    @Test
    public void testBoundOperation() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }


    //编程式事务
    @Test
    public void testTransactional() {

        Object o = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                //开启事务
                operations.multi();

                operations.opsForSet().add(redisKey, "张三");
                operations.opsForSet().add(redisKey, "李四");
                operations.opsForSet().add(redisKey, "王五");

                System.out.println(operations.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();
            }
        });

        System.out.println(o);
    }

    //统计20万个重复数据的独立总数
    @Test
    public void testHyberLogLog() {
        String redisKey = "test:hll:01";
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }
        for (int i = 1; i <= 100000; i++) {
            //Math.random()是令系统随机选取大于等于 0.0 且小于 1.0 的伪随机 double 值
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);//99553,精确值是100000
    }


    //将三组数据合并，再统计（有重复值）合并后的独立总数
    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);
        Long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);//19833,精确值是20000
    }

    //统计一组数据的布尔值

    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";
        //记录
        //是一段连续的内存空间相当于str,其他位置默认为false
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());

            }
        });
        System.out.println(obj);
    }

    //统计三组数据的布尔值，并对这三组数据做OR运算
    @Test
    public void testBitMapOperation(){
        String rediskey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(rediskey2, 0, true);
        redisTemplate.opsForValue().setBit(rediskey2, 1, true);
        redisTemplate.opsForValue().setBit(rediskey2, 2, true);


        String rediskey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(rediskey3, 2, true);
        redisTemplate.opsForValue().setBit(rediskey3, 3, true);
        redisTemplate.opsForValue().setBit(rediskey3, 4, true);

        String rediskey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(rediskey2, 4, true);
        redisTemplate.opsForValue().setBit(rediskey2, 5, true);
        redisTemplate.opsForValue().setBit(rediskey2, 6, true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes()
                        , rediskey2.getBytes(), rediskey3.getBytes(), rediskey4.getBytes());

                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,6));

    }
}
