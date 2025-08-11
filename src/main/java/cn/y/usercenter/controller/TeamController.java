package cn.y.usercenter.controller;

import cn.y.usercenter.common.BaseResponse;
import cn.y.usercenter.common.ErrorCode;
import cn.y.usercenter.exception.BusinessException;
import cn.y.usercenter.model.domain.Team;
import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.model.dto.TeamQuery;
import cn.y.usercenter.model.request.TeamAddRequest;
import cn.y.usercenter.model.request.TeamJoinRequest;
import cn.y.usercenter.model.request.TeamQuitRequest;
import cn.y.usercenter.model.request.TeamUpdateRequest;
import cn.y.usercenter.model.vo.TeamUserVO;
import cn.y.usercenter.service.TeamService;
import cn.y.usercenter.service.UserService;
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
import java.util.List;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:8080"},allowCredentials = "true")
@Slf4j
public class TeamController {


    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;


    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Team team = new Team();
        try {
            BeanUtils.copyProperties(teamAddRequest, team);
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

        return ResultUtils.success(teamList);
    }


    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Team team = new Team();
        try {
            BeanUtils.copyProperties(teamQuery,team);
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
    public BaseResponse<Boolean> dissolveTeam(@RequestBody long teamId, HttpServletRequest request){
        if(teamId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.dissolveTeam(teamId, loginUser);

        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");

        return ResultUtils.success(true);
    }
}
