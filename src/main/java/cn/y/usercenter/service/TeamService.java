package cn.y.usercenter.service;

import cn.y.usercenter.model.domain.Team;
import cn.y.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
