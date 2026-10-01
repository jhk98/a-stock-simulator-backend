package com.myproject.stocksimulator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisConnectionTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redisConnectionTest() {

        // 저장가
        redisTemplate.opsForValue().set("test", "hello redis");

        // 조회
        String value = redisTemplate.opsForValue().get("test");

        // 확인
        assertEquals("hello redis" ,value);

        // 삭제
        redisTemplate.delete("test");
    }
}