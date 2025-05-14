package com.yupi.usercenter.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSampel {

    public static void main(String[] args) {
        simpleRead();
    }

    /**
     * 最简单的读
     * <p>
     * 1. 创建excel对应的实体对象 参照{@link DemoData}
     * <p>
     * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器，参照{@link DemoDataListener}
     * <p>
     * 3. 直接读即可
     */
    public static void simpleRead() {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "F:\\java_workspace\\project\\user-center-backend-public\\src\\main\\resources\\userinfo.xlsx";
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, UserInfoData.class, new DemoDataListener()).sheet().doRead();


    }
}
