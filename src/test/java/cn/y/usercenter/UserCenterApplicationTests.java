package cn.y.usercenter;

import cn.y.usercenter.mapper.UserMapper;
import cn.y.usercenter.model.User;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@SpringBootTest
class UserCenterApplicationTests {

//    @Resource
//    private UserMapper userMapper;
//
//    @Test
//    void testDigest() throws NoSuchAlgorithmException {
//        String result = DigestUtils.md5DigestAsHex(("abc" + "mypassword").getBytes(StandardCharsets.UTF_8));
//        System.out.println(result);
//    }
//
//    @Test
//    void contextLoads() {
//        System.out.println("---selectAll---");
//        List<User> userList = userMapper.selectList(null);
//        Assertions.assertEquals(4,userList.size());
//        userList.forEach(System.out::println);
//    }

}
