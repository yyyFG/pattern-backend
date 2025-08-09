package cn.y.usercenter.service;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *  用户服务测试
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

//    @Resource
//    private UserService userService;
//
//    @Test
//    public void testAddUser(){
//        User user = new User();
//        user.setUsername("dsd");
//        user.setUserAccount("123");
//        user.setAvatarUrl("sadasd");
//        user.setGender(0);
//        user.setUserPassword("xxx");
//        user.setPhone("1233");
//        user.setEmail("456");
//        boolean result = userService.save(user);
//        System.out.println(user.getId());
//        assertTrue(result);
//    }
//
//
//    @Test
//    public void registerUser(){
//        String userAccount = "yyyy";
//        String userPassword = "";
//        String checkPassword = "123456";
//        String planetCode = "1";
//        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "yy";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "yyyy";
//        userPassword = "123456";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "yy yy";
//        userPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1,result);
//
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1,result);
//
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "ysensei";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1,result);
//
//
//
//        userAccount = "yyyyy";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertTrue(result > 0);
//
//    }
//
//    @Test
//    public void searchTags(){
//        List<String> tagsList = Arrays.asList("java","python","c++","男");
//        List<User> userList = userService.searchUserTags(tagsList);
//        Assert.assertNotNull(userList);
//    }
}