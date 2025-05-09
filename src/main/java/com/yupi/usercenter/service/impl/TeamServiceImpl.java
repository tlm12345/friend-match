package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TeamMapper;
import com.yupi.usercenter.model.domain.Team;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.model.domain.UserTeam;
import com.yupi.usercenter.model.enums.TeamStatusEnum;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.service.UserTeamService;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
* @author 12483
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-05-08 19:32:14
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User userLogin) {
        //        1. 校验请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
//        2. 验证是否登录
//        3. 对请求参数合法性校验
        {
            //        [1. 队伍名称不能为空，且长度小于20
            String teamName = team.getName();
            if (teamName == null || StringUtils.isBlank(teamName)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能为空!");
            }
            //        [2. 队伍描述长度不能大于1024
            String description = team.getDescription();
            if (StringUtils.isNotBlank(description) && description.length() > 1024){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长!");
            }
            //        [3. 队伍最大人数不能小于1，且不能大于20
            int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(1);
            if (maxNum < 1 && maxNum > 20){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍最大人数异常!");
            }
            //        [4. 过期时间不能在当前时间之前
            Date expireTime = team.getExpireTime();
            if (expireTime == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间未设置!");
            }
            if (expireTime.before(new Date())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间异常!");
            }
            //        [5. 状态不能为空，如果为空，则设置为0（公开）
            Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
            TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
            if (statusEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求!");
            }
            //        [6. 状态如果为加密，则必须要有密码，且密码的长度不能为空，要在5-10范围内。
            if (TeamStatusEnum.SECRETE.equals(statusEnum)){
                String password = team.getPassword();
                if (StringUtils.isBlank(password) || password.length() > 32){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确!");
                }
            }

        }
//        4. 检查用户是否达到创建队伍的上线。（普通用户最多创建5个队伍）
        final long userId = userLogin.getId();
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        long alreadyExist = this.count(wrapper);
        if (alreadyExist >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍数量过多!");
        }
//        5. 设置用户id到将要新建的队伍中，插入队伍信息到队伍表中
        team.setUserId(userId);
        boolean save = this.save(team);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常，插入失败!");
        }
        long teamId = team.getId();
//        6. 插入用户->队伍关系到用户队伍关系表中
        UserTeam userTeam = new UserTeam();
        userTeam.setId(null);
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        boolean save1 = userTeamService.save(userTeam);
        if (!save1){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常!");
        }

        return teamId;
    }
}




