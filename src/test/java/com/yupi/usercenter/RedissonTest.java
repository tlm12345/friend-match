package com.yupi.usercenter;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void RedissonClientTest(){

        RList<Object> aList = redissonClient.getList("tlm");
        aList.add("ok");
        aList.add("lm");

        Object res = aList.get(0);
        System.out.println((String) res);

    }
}
