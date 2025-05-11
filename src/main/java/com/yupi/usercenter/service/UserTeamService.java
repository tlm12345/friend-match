package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.domain.UserTeam;

import java.util.List;

/**
* @author 12483
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2025-05-08 19:32:14
*/
public interface UserTeamService extends IService<UserTeam> {

    List<Long> getUserTeamRelationByUserId(Long userId);

}
