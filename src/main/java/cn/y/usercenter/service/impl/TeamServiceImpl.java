package cn.y.usercenter.service.impl;

import cn.y.usercenter.common.ErrorCode;
import cn.y.usercenter.exception.BusinessException;
import cn.y.usercenter.mapper.UserTeamMapper;
import cn.y.usercenter.model.domain.Team;
import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.model.domain.UserTeam;
import cn.y.usercenter.model.enums.TeamEnumStatus;
import cn.y.usercenter.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.y.usercenter.service.TeamService;
import cn.y.usercenter.mapper.TeamMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Optional;

/**
* @author Youngman
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2025-08-07 15:13:26
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    // 建议引入 Service 不要用原始 Mapper，因为不知道 Mapper会不会有额外的校验，一般是 Service做校验，Mapper 很少检验
    @Resource
    private UserTeamService userTeamService;

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
}




