package com.yupi.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.common.ResultUtils;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.domain.Team;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.model.domain.request.TeamJoinRequest;
import com.yupi.usercenter.model.domain.request.TeamUpdateRequest;
import com.yupi.usercenter.model.dto.TeamQueryDTO;
import com.yupi.usercenter.model.vo.TeamUserVO;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.yupi.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 队伍接口
 *
 * @author tlm
 *
 */
@RestController
@RequestMapping("/team")
public class TeamController {
    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    /**
     * 创建队伍
     * @param team
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team, HttpServletRequest request){
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User userLogin = userService.getLoginUser(request);

        long teamId = teamService.addTeam(team, userLogin);

        return ResultUtils.success(teamId);

    }

    @GetMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(long teamId){

        boolean b = teamService.removeById(teamId);
        if (b == true) {
            return ResultUtils.success(b);
        }else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long teamId) {

        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return ResultUtils.success(team);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        boolean res = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return ResultUtils.success(true);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getTeams(TeamQueryDTO teamQueryDTO, HttpServletRequest request){
        if (teamQueryDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);
        List<TeamUserVO> teamUserVOS = teamService.listTeams(teamQueryDTO, isAdmin);

        return ResultUtils.success(teamUserVOS);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> getTeamsByPage(TeamQueryDTO teamQueryDTO){
        if (teamQueryDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamQueryDTO, team);
        QueryWrapper<Team> wrapper = new QueryWrapper<Team>(team);
        int pageSize = teamQueryDTO.getPageSize();
        int current = teamQueryDTO.getPageNum();
        Page<Team> page = new Page<>(current, pageSize);
        Page<Team> resPage = teamService.page(page, wrapper);
        return ResultUtils.success(resPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.joinTeam(teamJoinRequest, loginUser);
        return res == true ? ResultUtils.success(res) : ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
