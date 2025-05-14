package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.mapper.UserTeamMapper;
import com.yupi.usercenter.model.domain.UserTeam;
import com.yupi.usercenter.service.UserTeamService;

import org.springframework.stereotype.Service;

import javax.management.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 12483
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-05-08 19:32:14
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

    @Override
    public List<Long> getUserTeamRelationByUserId(Long userId) {
        if (userId == null) return null;

        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        List<UserTeam> resList = this.list(wrapper);
        Map<Long, List<UserTeam>> collect = resList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        return new ArrayList<>(collect.keySet());
    }
}




