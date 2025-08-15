package cn.y.usercenter.service;

import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author DCX
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-01-19 15:26:03
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return  脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 用户搜索标签
     * @param tagList
     * @return
     */
    List<User> searchUserTags(List<String> tagList);

    /**
     * 用户更新
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取最匹配用户
     * @param num
     * @param loginUser
     */
    List<User> matchUsers(long num, User loginUser);

    /**
     * 优化--获取最匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsersOptim(long num, User loginUser);

    /**
     * 优先队列--获取最匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsersOptimPrio(long num, User loginUser);
}
