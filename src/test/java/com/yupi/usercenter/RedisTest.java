package com.yupi.usercenter;
import java.util.Date;

import com.yupi.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource(name = "getRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedis(){

        System.out.println("使用的RedisTempalte" + redisTemplate);

        ValueOperations valueOperations = redisTemplate.opsForValue();

        valueOperations.set("ok", "tlm");
        valueOperations.set("test", 1);
        valueOperations.set("testtest", 1.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("tlm");

        valueOperations.set("object", user);

        Object stringValue = valueOperations.get("ok");
        Assert.assertTrue("tlm".equals((String) stringValue));
    }
}
