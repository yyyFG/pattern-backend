package cn.y.usercenter.service;

import cn.y.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

//    @Resource
//    private RedisTemplate redisTemplate;
//
//    @Test
//    void test(){
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        valueOperations.set("yString","dog");
//        valueOperations.set("yInt",1);
//        valueOperations.set("yDouble",2.0);
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("yyyy");
//        valueOperations.set("yUser",user);
//
//        Object yyy = valueOperations.get("yString");
//        Assertions.assertTrue("dog".equals((String)yyy));
//
//        yyy = valueOperations.get("yInt");
//        Assertions.assertTrue(1 == (Integer)yyy);
//
//        yyy = valueOperations.get("yDouble");
//        Assertions.assertTrue(2.0 == (Double)yyy);
//
//        System.out.println(valueOperations.get("yUser"));
//    }
}
