package com.yupi.usercenter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@SpringBootTest
public class ImportDataTest {

    @Resource
    UserService userService;

    @Resource
    UserMapper userMapper;

    final long INSERT_NUM = 10000L;

    @Test
    public void importData(){


        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        List<User> userList = new LinkedList<User>();

        for (long i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setId(null);
            user.setUsername("tlm");
            user.setUserAccount("123456789");
            user.setAvatarUrl("https://tse2-mm.cn.bing.net/th/id/OIP-C.cn_mIqJN0Td_0Ono0xMEsQHaLL?rs=1&pid=ImgDetMain");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("13432532");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("555555555");
            user.setTags("[]");

            userMapper.insert(user);
        }

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void asyncImportData(){
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        List<User> userList = new LinkedList<User>();
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        for(int j = 0; j < 10; j++){
            List<User> tempUserList = new LinkedList<>();
            while(true){
                User user = new User();
                user.setId(null);
                user.setUsername("tlm");
                user.setUserAccount("123456789");
                user.setAvatarUrl("https://tse2-mm.cn.bing.net/th/id/OIP-C.cn_mIqJN0Td_0Ono0xMEsQHaLL?rs=1&pid=ImgDetMain");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("13432532");
                user.setEmail("123@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("555555555");
                user.setTags("[]");

                tempUserList.add(user);
                if (tempUserList.size() % 1000 == 0){
                    break;
                }
            }

            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                userService.saveBatch(tempUserList, 1000);
            });
            futureList.add(voidCompletableFuture);
        }



        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
