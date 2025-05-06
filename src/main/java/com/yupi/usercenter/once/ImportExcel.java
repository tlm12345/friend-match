package com.yupi.usercenter.once;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ImportExcel {

    public static void main(String[] args) {
        System.out.println("log");
        String fileName = "F:\\java_workspace\\project\\user-center-backend-public\\src\\main\\resources\\userinfo.xlsx";
        synchronousRead(fileName);
    }

    public static void simpleRead(String fileName) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, UserInfoData.class, new DemoDataListener()).sheet().doRead();


    }
    /**
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserInfoData> list = EasyExcel.read(fileName).head(UserInfoData.class).sheet().doReadSync();
        Map<String, List<UserInfoData>> listMap = list.stream().collect(Collectors.groupingBy(UserInfoData::getUsername));
        log.info("总计 : " + listMap.keySet().size());


    }
}
