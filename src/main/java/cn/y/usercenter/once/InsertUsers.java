package cn.y.usercenter.once;


import cn.y.usercenter.mapper.UserMapper;
import cn.y.usercenter.model.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * 导入用户任务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

//    @Scheduled(fixedDelay = 5000)
    public void InsertUser(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int Insert_Num = 10000000;
        for (int i = 0; i < Insert_Num; i++) {
            User user = new User();
            user.setUsername("faker");
            user.setUserAccount("fakeyyy");
            user.setAvatarUrl("https://alist.yyyai.xyz/d/picture/1739951906017.jpg?sign=QKWJRkbZQt9DtU8vjpJK5Xs_ogiRchV_XcZdjG9MqNg=:0");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setPhone("123456");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1111");
            user.setTags("[]");
//            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
