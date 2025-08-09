package cn.y.usercenter.controller;

import cn.y.usercenter.common.BaseResponse;
import cn.y.usercenter.common.ErrorCode;
import cn.y.usercenter.exception.BusinessException;
import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.model.request.UserLoginRequest;
import cn.y.usercenter.model.request.UserRegisterRequest;
import cn.y.usercenter.service.UserService;
import cn.y.usercenter.utils.ResultUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.y.usercenter.constant.UserConstant.ADMIN_ROLE;
import static cn.y.usercenter.constant.UserConstant.USER_LOGIN_STATE;


/**
 *  用户接口
 *
 * @author yyy
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:8080"},allowCredentials = "true")
@Slf4j
//@CrossOrigin(origins = {"http://localhost:3000","http://user.yyyai.xyz"},methods = {RequestMethod.POST,RequestMethod.GET}, allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
//            return ResultUtils.error(ErrorCode.NULL_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        return new BaseResponse<>(0,result,"ok");
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAllBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, request);
//        return new BaseResponse<>(0,user,"ok");
        return ResultUtils.success(user) ;
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User)userObj;
        if(currentUser == null) throw new BusinessException(ErrorCode.NO_LOGIN);

        long userId = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);

        return ResultUtils.success(safetyUser);
    }


    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        // 1. 校验参数是否为空
        if(user == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 2. 校验权限
        // 3. 触发更新
        Integer result = userService.updateUser(user, loginUser);

        return ResultUtils.success(result);
    }

    @GetMapping("/search/list")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagsList){
        if(CollectionUtils.isEmpty(tagsList)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        List<User> userList = userService.searchUserTags(tagsList);

        return ResultUtils.success(userList);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> usersSearch(String username, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        return ResultUtils.success(result);
//        return userList.stream().map(user -> {
//            user.setUserPassword(null);
//            return user;
//        }).collect(Collectors.toList());

    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> usersRecommend(long pageSize, long pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("yupao:user:recommend:%s",loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if(userPage != null){
            return ResultUtils.success(userPage);
        }

        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        Page<User> userList = userService.page(new Page<>((pageNum - 1) * pageSize, pageSize), queryWrapper);
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 写入缓存
        try {
            valueOperations.set(redisKey, userPage, 60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }

        return ResultUtils.success(userPage);
//        return userList.stream().map(user -> {
//            user.setUserPassword(null);
//            return user;
//        }).collect(Collectors.toList());

    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> userDelete(@RequestBody long id, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        // 仅管理员查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
