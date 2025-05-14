package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.domain.Team;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.model.domain.request.TeamJoinRequest;
import com.yupi.usercenter.model.domain.request.TeamQuitRequest;
import com.yupi.usercenter.model.domain.request.TeamUpdateRequest;
import com.yupi.usercenter.model.dto.TeamQueryDTO;
import com.yupi.usercenter.model.vo.TeamUserVO;

import java.util.List;


/**
* @author 12483
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-05-08 19:32:14
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User userLogin);

    List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDTO, User userLogin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User userLogin);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User userLogin);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long teamId, User loginUser);
}
