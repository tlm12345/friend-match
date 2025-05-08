package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.domain.Team;
import com.yupi.usercenter.model.domain.User;


/**
* @author 12483
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-05-08 19:32:14
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User userLogin);

}
