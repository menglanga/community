package com.nowcoder.community.dao;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("alphaMybatis")
@Primary//优先级
public class AlphaMybatisImpl implements AlphaDao {
    @Override
    public String select() {
        return "Mybatis";
    }
}
