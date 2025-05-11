package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TeamMapper;
import com.yupi.usercenter.model.domain.Team;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.model.domain.UserTeam;
import com.yupi.usercenter.model.domain.request.TeamJoinRequest;
import com.yupi.usercenter.model.domain.request.TeamQuitRequest;
import com.yupi.usercenter.model.domain.request.TeamUpdateRequest;
import com.yupi.usercenter.model.dto.TeamQueryDTO;
import com.yupi.usercenter.model.enums.TeamStatusEnum;
import com.yupi.usercenter.model.vo.TeamUserVO;
import com.yupi.usercenter.model.vo.UserVO;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.service.UserTeamService;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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

    @Resource
    private UserService userService;



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

    @Override
    public List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDTO, boolean isAdmin) {
//        1. 校验请求参数是否为空
        if (teamQueryDTO == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
//        2. 依据请求参数构造查询条件
        {
            String name = teamQueryDTO.getName();
            if (StringUtils.isNotBlank(name)){
               wrapper.like("name", name);
            }

            String description = teamQueryDTO.getDescription();
            if (StringUtils.isNotBlank(description)){
                wrapper.like("description", description);
            }

            Integer maxNum = teamQueryDTO.getMaxNum();
            if (maxNum != null){
                wrapper.eq("maxNum", maxNum);
            }

            Long userId = teamQueryDTO.getUserId();
            if (userId != null){
                wrapper.eq("userId", userId);
            }

            List<Long> idsList = teamQueryDTO.getIds();
            if (!CollectionUtils.isEmpty(idsList)){
                wrapper.in("id", idsList);
            }
        }
//           1. 只有管理员才能够查找私密和加密队伍
        if (!isAdmin) {
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.PUBLIC;
            wrapper.eq("status", teamStatusEnum.getStatus());
        }

//           2. 不展示已过期的队伍
        wrapper.and(wp -> wp.gt("expireTime", new Date()));
//           3. 允许通过关键字同时对队伍名称和队伍描述进行查找。
        String searchText = teamQueryDTO.getSearchText();
        if (StringUtils.isNotBlank(searchText) && isAdmin) {
            wrapper.and(wq ->
                wq.like("name", searchText).or().like("description", searchText)
            );
        }

        List<Team> teamList = this.list(wrapper);
//        3. 关联创建队伍的用户
        List<TeamUserVO> responseList = new LinkedList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            User tempUser = userService.getById(userId);

            if (tempUser == null){
                continue;
            }

            TeamUserVO teamUserVO = new TeamUserVO();
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            BeanUtils.copyProperties(tempUser, userVO);
            teamUserVO.setCreateUser(userVO);

            responseList.add(teamUserVO);
        }

        return responseList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User userLogin) {
//        1. 校验参数是否为空, 校验要修改的队伍是否存在
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (userLogin == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Long teamId = teamUpdateRequest.getId();
        if (teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "缺少要修改队伍的ID");
        }

        Team oldTeam = this.getById(teamId);
        if (oldTeam == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        Long userId = userLogin.getId();
//        2. 只有管理员和队伍的创建者可以修改
        if (!userService.isAdmin(userLogin) && !userId.equals(oldTeam.getUserId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
//        3. 允许修改的信息有：名称，描述，过期时间，状态，密码
//          1. 新的过期时间必须大于当前时间
        Date newExpireTime = teamUpdateRequest.getExpireTime();
        if (newExpireTime != null){
            if (newExpireTime.before(new Date())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间必须要在当前时间之后");
            }
        }

        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnum teamStatusByValue = TeamStatusEnum.getTeamStatusByValue(status);
        if (TeamStatusEnum.SECRETE.equals(teamStatusByValue)){
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "私密房间必须设置密码");
            }
        }else{
            teamUpdateRequest.setPassword("");
        }
//          2. 新的状态如果和旧状态一致则不修改。如果从加密状态变为公开或私密，需要清除密码。如果从公开或私密变为加密，必须传入密码。密码需要加盐。
        Team newTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, newTeam);

        boolean res = this.updateById(newTeam);
        return res;
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User userLogin) {
//        1. 用户需要保持登录态， 并且所加入的队伍需要存在
        Long expectedJoinTeamId = teamJoinRequest.getId();
        Team targetTeam = this.getById(expectedJoinTeamId);
        if (targetTeam == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
//        2. 用户不能重复加入队伍。（幂等性）
        Long userId = userLogin.getId();
        if (userId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Long targetTeamId = targetTeam.getId();
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId).eq("teamId", targetTeamId);
        UserTeam userTeamRecord = userTeamService.getOne(wrapper);
        if (userTeamRecord != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不能重复加入同一个队伍");
        }
//        3. 用户最多加入5个队伍，且加入的队伍必须是未满员，未过期的。
        QueryWrapper<UserTeam> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("userId", userId);
        long count = userTeamService.count(wrapper1);
        if (count >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多能加入5个队伍");
        }
        QueryWrapper<UserTeam> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("teamId", targetTeamId);
        long currentNumber = userTeamService.count(wrapper2);
        Integer maxNum = targetTeam.getMaxNum();
        if (currentNumber >= maxNum){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "房间人数已满");
        }
        Date expireTime = targetTeam.getExpireTime();
        if (expireTime == null || expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期,不可加入");
        }
//        4. 用户不能加入私密队伍。
        Integer status = targetTeam.getStatus();
        TeamStatusEnum teamStatusByValue = TeamStatusEnum.getTeamStatusByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusByValue)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不可加入私密房间");
        }
//        5. 如果用户加入加密队伍，则需要输入密码和队伍密码匹配。
        String userPassword = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRETE.equals(teamStatusByValue)){
            if (StringUtils.isBlank(userPassword) || !userPassword.equals(targetTeam.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
//        6. 新增队伍-用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(targetTeamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
//        1. 校验请求参数
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
//        2. 校验队伍是否存在，校验用户是否加入了该队伍
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "退出队伍Id为空");
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
//        3. 如果队伍
        QueryWrapper<UserTeam> userTeamWrapper = new QueryWrapper<>();
        userTeamWrapper.eq("teamId", teamId);

        Long userId = loginUser.getId();
        long count = userTeamService.count(userTeamWrapper);
//          1. 只剩下一人。且这个人就是队伍拥有者。直接删除该队伍
        if (count == 1){
            if(userId == team.getUserId()){
                this.removeById(teamId);
            }else{
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
        }else if (count > 1){
//          2. 还剩下多人。
//              1. 如果是队长退出，，则队伍拥有人顺位给下一个用户。
            if (userId.equals(team.getUserId())) {
                userTeamWrapper.orderByAsc("id").last("limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamWrapper);
                Team newTeam = new Team();
                UserTeam secondUserTeam = userTeamList.get(1);
                Long newUserId = secondUserTeam.getUserId();
                if (newUserId == null) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                newTeam.setUserId(newUserId);
                newTeam.setId(teamId);
                boolean res = this.updateById(newTeam);
                if (res == false) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

//      4. 删除用户-队伍关系。
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("teamId", teamId).eq("userId", userId);
        boolean remove = userTeamService.remove(wrapper);

        return remove;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser) {
//        1. 校验请求参数
        // 参数可以不校验
//        2. 校验队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
//        3. 判断要删除的队伍的拥有者是否是当前用户
        Long userId = loginUser.getId();
        if (!userId.equals(team.getUserId())) throw new BusinessException(ErrorCode.NO_AUTH);
//        4. 根据判断条件，删除队伍，同时删除所有的用户-队伍关系。
        //删除队伍
        boolean res1 = this.removeById(teamId);
        if (!res1) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        // 删除所有相关的用户-队伍关系
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("teamId", teamId);
        boolean res2 = userTeamService.remove(wrapper);
        if (!res2) throw new BusinessException(ErrorCode.SYSTEM_ERROR);

        return true;
    }

}




