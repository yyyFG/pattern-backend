package cn.y.usercenter.service.impl;

import cn.y.usercenter.common.ErrorCode;
import cn.y.usercenter.exception.BusinessException;
import cn.y.usercenter.model.vo.UserVO;
import cn.y.usercenter.utils.AlgorithmUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.service.UserService;
import cn.y.usercenter.mapper.UserMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.y.usercenter.constant.UserConstant.ADMIN_ROLE;
import static cn.y.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author DCX
* @description 用户服务实现类
* @createDate 2025-01-19 15:26:02
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     *  盐值，混淆密码
     */
    private static final String SALT = "yyy";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
//        if(userAccount == null || userPassword == null || checkPassword == null) ==
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword,planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if(planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
//        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"包含特殊字符");
        }
        // 密码和校验密码不相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不相同");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
//        long count = this.count(queryWrapper);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户重复");
        }

        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
//        long count = this.count(queryWrapper);
        count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号重复");
        }

        // 2. 加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        // 3. 插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult){
            return -1;
        }
        return user.getId();
    }


    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if(StringUtils.isAllBlank(userAccount,userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码不能为空");
        }
        if(userAccount.length() < 4){
            return null;
        }
        if(userPassword.length() < 8){
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return null;
        }

        // 账户是否存在
        // 2. 加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if(user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);

        // 4. 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);

        return safetyUser;
    }


    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        if(originUser == null) return null;

        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());

        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 标签查询搜索用户 内存过滤
     * @param tagList
     * @return
     */
    @Override
    public List<User> searchUserTags(List<String> tagList){
        if(CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 内存查询
        // 1，先查询所有用户
        QueryWrapper<User> queryWrapper2 = new QueryWrapper<>();
//        userMapper.selectCount(null);

        List<User> userList = userMapper.selectList(queryWrapper2);
        Gson gson = new Gson();
        // 2.在内存中判断是否包含要求的标签
//        for(User user : userList){  // 可以用 API 来写这段代码
//            String tagNameStr = user.getTags();
//            Set<String> tempTagList = gson.fromJson(tagNameStr, new TypeToken<Set<String>>() {}.getType());
//            for(String tagName : tempTagList){
//                if(!tempTagList.contains(tagName)) return false;
//            }
//            return true;
//        }

        return userList.stream().filter(user -> {
            String tagNameStr = user.getTags();
            if(StringUtils.isBlank(tagNameStr)){
                return false;
            }
            Set<String> tempTagListSet = gson.fromJson(tagNameStr, new TypeToken<Set<String>>() {}.getType());
            tempTagListSet = Optional.ofNullable(tempTagListSet).orElse(new HashSet<>());
            for(String tagName : tagList){
                if(!tempTagListSet.contains(tagName)) return false;
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 用户更新
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if(userId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null) throw new BusinessException(ErrorCode.NULL_ERROR);

        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);

        if(userObj == null) throw new BusinessException(ErrorCode.NO_AUTH);

        return (User)userObj;
    }


    /**
     * 标签查询搜索用户 SQL版
     * @param tagList
     * @return
     */
    @Deprecated
    public List<User> searchUserTagsBySQL(List<String> tagList){
        if(CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long startTime = System.currentTimeMillis();
        // sql 语句查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like ‘%Python%’
        for(String tagName : tagList){
            queryWrapper = queryWrapper.like("tags", tagName);

        }
        List<User> userList = userMapper.selectList(queryWrapper);
        log.info("sql query time: " + (System.currentTimeMillis() - startTime));

        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    public boolean isAdmin(User loginUser){
        // 仅管理员查询
        if(loginUser.getUserRole() != ADMIN_ROLE) return false;
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取最匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        List<User> userList = this.list();
        String userTags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
        }.getType());

        // 用户列表的下表 =》相似度
        SortedMap<Integer, Long> indexDistanceMap = new TreeMap<>();
        for (int i = 0; i < tagList.size(); i++) {
            User user = userList.get(i);
            String tags = user.getTags();
            // 无标签
            if(StringUtils.isBlank(tags)){
                continue;
            }
            List<String> userTagsList = gson.fromJson(tags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagsList);
            indexDistanceMap.put(i, distance);
        }
        List<Integer> maxDistanceIndexList = indexDistanceMap.keySet().stream().limit(num).collect(Collectors.toList());
        List<User> userVOList = maxDistanceIndexList.stream()
                .map(index -> getSafetyUser(userList.get(index)))
                .collect(Collectors.toList());

        return userVOList;
    }

    /**
     * 获取最匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsersOptim(long num, User loginUser) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id","tags");
        List<User> userList = this.list(queryWrapper);

        String userTags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
        }.getType());

        // 用户列表的下表 =》相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算当前用户和所有用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String tags = user.getTags();
            // 无标签 或 当前用户为自己
            if(StringUtils.isBlank(tags) || Objects.equals(user.getId(), loginUser.getId())){
                continue;
            }
            List<String> userTagsList = gson.fromJson(tags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagsList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离有小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a,b) -> (int)(a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());

        // 有顺序的 userId 列表
        List<Long> userListVO = topUserPairList.stream()
                .map(pari -> pari.getKey().getId()).collect(Collectors.toList());

        // 根据 id 查询 user 完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userListVO);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));

        // 因为上面查询打乱了顺序，这里根据上面有序的 userId 列表赋值
        List<User> finalUserList = new ArrayList<>();
        for(Long userId : userListVO){
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }

//        return getFinalUsersBySql(userListVO);
//        return getFinalUsersByMemory(userListVO);

        return finalUserList;
    }

    /**
     * 获取最匹配用户 -- 优先队列
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsersOptimPrio(long num, User loginUser) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id","tags");
        List<User> userList = this.list(queryWrapper);

        String userTags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
        }.getType());

        // 用户列表的下表 =》相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        PriorityQueue priorityQueue = new PriorityQueue<>(Comparator.comparingLong(Pair::getValue));

        // 依次计算当前用户和所有用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String tags = user.getTags();
            // 无标签 或 当前用户为自己
            if(StringUtils.isBlank(tags) || Objects.equals(user.getId(), loginUser.getId())){
                continue;
            }
            List<String> userTagsList = gson.fromJson(tags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagsList);
//            list.add(new Pair<>(user, distance));

            if(priorityQueue.size() < num){
                priorityQueue.add(new Pair<>(user, distance));
            }else if(distance < priorityQueue.peek().getValue()){
                priorityQueue.poll();
                priorityQueue.add(new Pair<>(user, distance));
            }
        }

        List userIdList = new ArrayList<>();
        while (!priorityQueue.isEmpty()){
            userIdList.add(priorityQueue.poll().getKey().getId());
        }

        // 根据 id 查询 user 完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, User> userIdUserMap = this.list(userQueryWrapper)
                .stream()
                .collect(Collectors.toMap(User::getId, this::getSafetyUser));

        // 因为上面查询打乱了顺序，这里根据上面有序的 userId 列表赋值
        List<User> finalUserList = new ArrayList<>();
        for(Long userId : userIdList){
            finalUserList.add(userIdUserMap.get(userId));
        }

//        return getFinalUsersBySql(userListVO);
//        return getFinalUsersByMemory(userListVO);

        return finalUserList;
    }

    /**
     * 根据 id 顺序集合获取用户组（sql版）
     * @param userListVO
     * @return
     */
    private List<User> getFinalUsersBySql(List<Long> userListVO) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        // 使用 sql 拼接实现 list user in userListVO orderByField userListVO
        userQueryWrapper.in("id", userListVO);
        userQueryWrapper.last("order by field(id," + StringUtils.join(userListVO, ",") + ")");
        return this.list(userQueryWrapper).stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据 id 顺序集合获取用户组（内存版）
     * @param userListVO
     * @return
     */
    private List<User> getFinalUsersByMemory(List<Long> userListVO){
        // 根据 id 查询user 完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userListVO);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));

        // 因为上面查询打乱了顺序，这里根据上面有序的 userID 列表赋值
        List<User> finalUserList = new ArrayList<>();
        for(Long userId : userListVO){
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }

        return finalUserList;
    }
}




