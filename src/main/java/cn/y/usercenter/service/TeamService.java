package cn.y.usercenter.service;

import cn.y.usercenter.model.domain.Team;
import cn.y.usercenter.model.domain.User;
import cn.y.usercenter.model.dto.TeamQuery;
import cn.y.usercenter.model.request.TeamJoinRequest;
import cn.y.usercenter.model.request.TeamQuitRequest;
import cn.y.usercenter.model.request.TeamUpdateRequest;
import cn.y.usercenter.model.vo.TeamUserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
* @author Youngman
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2025-08-07 15:13:26
*/
public interface TeamService extends IService<Team> {


    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 队伍列表查询
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) throws InvocationTargetException, IllegalAccessException;

    /**
     * 队伍更新
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) throws InvocationTargetException, IllegalAccessException;

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    boolean dissolveTeam(long teamId, User loginUser);
}
