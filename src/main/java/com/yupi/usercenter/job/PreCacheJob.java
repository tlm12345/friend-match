package com.yupi.usercenter.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource(name = "getRedisTemplate")
    private RedisTemplate redisTemplate;

    private List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "0 10 20 * * *")
    public void preCacheUserRecommend(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String keyTemplate = "friendMatch:user:recommend:%s";

        for (Long aLong : mainUserList) {
            String key = String.format(keyTemplate, aLong);
            User user = new User();
            user.setId(aLong);
            Page<User> recommendUser = userService.getRecommendUser(user, 1, 8);

            try {
                valueOperations.set(key, recommendUser, 1, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error("PreCacheJob had failed!!!", e);
            }

        }
    }

}
