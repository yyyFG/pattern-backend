package cn.y.usercenter.service;

import org.springframework.boot.test.context.SpringBootTest;

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
