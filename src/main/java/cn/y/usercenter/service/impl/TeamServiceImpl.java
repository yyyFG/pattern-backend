package cn.y.usercenter.service.impl;

import cn.y.usercenter.common.ErrorCode;
import cn.y.usercenter.exception.BusinessException;
import cn.y.usercenter.mapper.UserTeamMapper;
import cn.y.usercenter.model.domain.Team;
import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.model.domain.UserTeam;
import cn.y.usercenter.model.dto.TeamQuery;
import cn.y.usercenter.model.enums.TeamEnumStatus;
import cn.y.usercenter.model.request.TeamJoinRequest;
import cn.y.usercenter.model.request.TeamQuitRequest;
import cn.y.usercenter.model.request.TeamUpdateRequest;
import cn.y.usercenter.model.vo.TeamUserVO;
import cn.y.usercenter.model.vo.UserVO;
import cn.y.usercenter.service.UserService;
import cn.y.usercenter.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.y.usercenter.service.TeamService;
import cn.y.usercenter.mapper.TeamMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author Youngman
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2025-08-07 15:13:26
*/
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    // 建议引入 Service 不要用原始 Mapper，因为不知道 Mapper会不会有额外的校验，一般是 Service做校验，Mapper 很少检验
    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if(team == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //2. 是否登录，未登录不允许创建
        if(loginUser == null) throw new BusinessException(ErrorCode.NO_AUTH, "请登录");
        final long userId = loginUser.getId();
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        int teamNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(teamNum < 1 || teamNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数得 > 1 且 <= 20");
        }
        //  b. 队伍标题 <= 20
        String teamName = team.getName();
        if(StringUtils.isBlank(teamName) || teamName.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不能为空或过长");
        }
        //  c. 描述 <= 512
        String teamDescription = team.getDescription();
        if(StringUtils.isBlank(teamDescription) &&   teamDescription.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不能为空或过长");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamEnumStatus enumStatus = TeamEnumStatus.getEnumByValue(status);
        if(enumStatus == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getTeamPassword();
        if(TeamEnumStatus.SECRET.equals(enumStatus)){
            if(StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空或过长");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //  g. 校验用户最多创建 5 个队伍
        // todo 有bug，可能同时创建 100 个队伍
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userId",userId);
        long hasTeamCount = this.count(queryWrapper);
        if(hasTeamCount >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setId(null);
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) throws InvocationTargetException, IllegalAccessException {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                queryWrapper.eq("id", id);
            }
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }

            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description", description);
            }
            // 查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0 && maxNum <= 5){
                queryWrapper.eq("maxNum", maxNum);
            }
            // 根据创建人 id 来查询队伍
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0){
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamEnumStatus teamEnumStatus = TeamEnumStatus.getEnumByValue(status);
            if(teamEnumStatus == null){
                teamEnumStatus = TeamEnumStatus.PUBLIC;
            }
            // 只有管理员才能查看加密还有不公开的队伍
            if(!isAdmin && !teamEnumStatus.equals(TeamEnumStatus.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }

            queryWrapper.eq("status", teamEnumStatus.getValue());
        }
        // 不展示已过期的队伍
        // expireTime is not null and expireTime < now()
        queryWrapper.and(qw -> qw.gt("expireTime",new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        // 关联查询用户信息
        // 1. 自己写 SQL
        // 查询队伍和创建人的信息
        // select * from team t left join user u on t.userId = u.id
        // 查询队伍和已加入队伍成员的信息
        // select * from team t join user_team ut on t.id = ut.teamId

        // 关联查询创建人的用户信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for(Team team : teamList){
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);

            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(teamUserVO, team);
            // 脱敏用户信息
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(userVO, user);
                teamUserVO.setCurrentUser(userVO);

            }
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team oldTeam = this.getById(teamUpdateRequest.getId());
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }

        // 只有管理员或者队伍的创建者可以修改
        if(oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        TeamEnumStatus teamEnumStatus = TeamEnumStatus.getEnumByValue(teamUpdateRequest.getStatus());
        if(teamEnumStatus.equals(TeamEnumStatus.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getTeamPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不能为空");
            }
        }

        // 传值为空时
        if(StringUtils.isBlank(teamUpdateRequest.getName())){
            teamUpdateRequest.setName(oldTeam.getName());
        }
        if(StringUtils.isBlank(teamUpdateRequest.getDescription())){
            teamUpdateRequest.setDescription(oldTeam.getDescription());
        }

        Team updateTeam = new Team();

        try {
            BeanUtils.copyProperties(oldTeam, teamUpdateRequest);
            BeanUtils.copyProperties(updateTeam, oldTeam);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) throws InvocationTargetException, IllegalAccessException {
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = this.getTeamById(teamId);

        Date expireTime = team.getExpireTime();
        if(expireTime != null && new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }

        Integer status = team.getStatus();
        TeamEnumStatus teamEnumStatus = TeamEnumStatus.getEnumByValue(status);
        if(TeamEnumStatus.PRIVATE.equals(teamEnumStatus)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        if(TeamEnumStatus.SECRET.equals(teamEnumStatus)){
            if(StringUtils.isBlank(teamJoinRequest.getTeamPassword()) || !teamJoinRequest.getTeamPassword().equals(team.getTeamPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 该用户已加入的队伍
        long userId = loginUser.getId();
        if (team.getUserId() == userId){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入自己的队伍");
        }

        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long teamCount = userTeamService.count(queryWrapper);
        if(teamCount > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
        }

        // 已加入队伍人数
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("teamId", team.getId());
//        long teamHasJoin = userTeamService.count(queryWrapper);
        long teamHasJoin = this.countTeamUserByTeamId(team.getId());
        if(teamHasJoin > team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", team.getId()).eq("userId", userId);
        long teamRepeat = userTeamService.count(queryWrapper);
        if(teamRepeat > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入队伍");
        }

        // 修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamQuitRequest.getTeamId();
        Team team = this.getTeamById(teamId);

        Long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(queryWrapper);
        if(count < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有加入此队伍");
        }

        long hasJoinNum = this.countTeamUserByTeamId(teamId);

        // 队伍只剩一人，队伍解散
        if(hasJoinNum == 1){
            boolean b1 = this.removeById(teamId);
            boolean b2 = userTeamService.remove(queryWrapper);
            return b1 && b2;
        } else {
//            if(team.getUserId().equals(userId)){
            // 队伍解散
//                boolean b1 = this.removeById(teamId);
//                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
//                queryWrapper.eq("teamId", teamId);
//                boolean b2 = userTeamService.remove(userTeamQueryWrapper);
//                return b1 && b2;
//            }
            // 队伍还剩两人
            // 是为队长
            if(team.getUserId().equals(userId)){
                // 把队伍转移给第二个人
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", teamId);
//                queryWrapper.orderByAsc("id");
                queryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR);
                }

                UserTeam userSecond = userTeamList.get(1);
                Long nextTeamLeaderId = userSecond.getUserId();
                // 更新当前队长
                team.setUserId(nextTeamLeaderId);
                boolean b1 = this.updateById(team);
                if(!b1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
                }

                boolean b2 = userTeamService.removeById(userId);
                if(!b2){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
                }

                return true;

                // 不是队长
            } else {
                return userTeamService.remove(queryWrapper);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dissolveTeam(long teamId, User loginUser) {
        Team team = this.getTeamById(teamId);

        Long userId = loginUser.getId();
        if(!team.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH, "不是该队伍队长");
        }

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", team.getId());
        // 移除所有加入队伍的关联信息
        boolean b1 = userTeamService.remove(userTeamQueryWrapper);
        if(!b1){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        boolean b2 = this.removeById(team);

        return b1 && b2;
    }


    /**
     * 获取当前用户队伍人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        return userTeamService.count(queryWrapper);
    }

    /**
     * 根据 id 获取当前队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId){
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }

        return team;
    }
}




