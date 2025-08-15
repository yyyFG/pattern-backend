package cn.y.usercenter.controller;

import cn.y.usercenter.common.BaseResponse;
import cn.y.usercenter.common.DeleteRequest;
import cn.y.usercenter.common.ErrorCode;
import cn.y.usercenter.exception.BusinessException;
import cn.y.usercenter.model.domain.Team;
import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.model.domain.UserTeam;
import cn.y.usercenter.model.dto.TeamQuery;
import cn.y.usercenter.model.request.TeamAddRequest;
import cn.y.usercenter.model.request.TeamJoinRequest;
import cn.y.usercenter.model.request.TeamQuitRequest;
import cn.y.usercenter.model.request.TeamUpdateRequest;
import cn.y.usercenter.model.vo.TeamUserVO;
import cn.y.usercenter.service.TeamService;
import cn.y.usercenter.service.UserService;
import cn.y.usercenter.service.UserTeamService;
import cn.y.usercenter.utils.ResultUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:8080"},allowCredentials = "true")
@Slf4j
public class TeamController {


    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;


    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamAddRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        User loginUser = userService.getLoginUser(request);
        long teamId = teamService.addTeam(team, loginUser);

        return ResultUtils.success(teamId);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long teamId){
        if(teamId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        boolean result = teamService.removeById(teamId);

        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");

        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);

        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");

        return ResultUtils.success(true);
    }

    @GetMapping("/search")
    public BaseResponse<Team> getTeamById(long id) {
        if(id <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Team team = teamService.getById(id);

        if(team == null) throw new BusinessException(ErrorCode.NULL_ERROR);

        return ResultUtils.success(team);
    }

//    @GetMapping("/list")
//    public BaseResponse<List<Team>> listTeams(TeamQuery teamQuery){
//        if(teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
//
//        Team team = new Team();
//        try {
//            BeanUtils.copyProperties(team,teamQuery);
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//        }
//
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        List<Team> teamList = teamService.list(queryWrapper);
//
//        return ResultUtils.success(teamList);
//    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        if(teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);

        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);

        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.eq("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入队伍的 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });

        }catch (Exception e){}

        return ResultUtils.success(teamList);
    }

    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        if(teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
//        boolean isAdmin = userService.isAdmin(loginUser);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);

        return ResultUtils.success(teamList);
    }


    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        if(teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 id
        // teamId userId
        // 1，2
        // 1，3
        // 2，3
        // result
        // 1 =》2，3
        // 2 =》3
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);

        return ResultUtils.success(teamList);
    }


    //todo 查询分页
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        Page<Team> teamPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(teamPage, queryWrapper);

        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);

        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);

        return ResultUtils.success(result);
    }

    @PostMapping("/dissolveTeam")
    public BaseResponse<Boolean> dissolveTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest == null || deleteRequest.getId() <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.dissolveTeam(deleteRequest.getId(), loginUser);

        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");

        return ResultUtils.success(true);
    }

}
