package com.yupi.usercenter;

import com.yupi.usercenter.common.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AlgorithmUtilsTest {

    @Test
    public void test() {
        List<String> l1 = Arrays.asList("java", "大二", "乒乓球");
        List<String> l2 = Arrays.asList("java", "大四", "篮球");

        int minD = AlgorithmUtils.minDistance(l1, l2);
        System.out.println(minD);
    }
}
