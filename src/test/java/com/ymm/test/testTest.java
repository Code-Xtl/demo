package com.ymm.test;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class testTest {

    @Test
    public void test1() {
        Cache<String,String> cache = CacheBuilder.newBuilder()
                .maximumSize(100) //设置缓存最大容量
                .expireAfterWrite(1, TimeUnit.MINUTES) //过期策略，写入一分钟后过期
                .build();
        cache.put("a","a1");
        String value = cache.getIfPresent("a");
    }
}