package com.yupi.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.DeleteRequest;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.common.ResultUtils;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.domain.Team;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.model.domain.UserTeam;
import com.yupi.usercenter.model.domain.request.TeamJoinRequest;
import com.yupi.usercenter.model.domain.request.TeamQuitRequest;
import com.yupi.usercenter.model.domain.request.TeamUpdateRequest;
import com.yupi.usercenter.model.dto.TeamQueryDTO;
import com.yupi.usercenter.model.vo.TeamUserVO;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.service.UserTeamService;
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

    @Resource
    private UserTeamService userTeamService;

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


    @GetMapping("/get")
    public BaseResponse<Team> getTeam(@RequestParam("id") Long teamId) {

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
        List<TeamUserVO> teamUserVOS = teamService.listTeams(teamQueryDTO, loginUser);

        return ResultUtils.success(teamUserVOS);
    }

    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> getICreateTeams(TeamQueryDTO teamQueryDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);

        teamQueryDTO.setUserId(loginUser.getId());
        // 临时给予管理员权限。这么做是为了复用代码。因为非管理员，无法查看私密队伍。
        List<TeamUserVO> teamUserVOS = teamService.listTeams(teamQueryDTO, loginUser);

        return ResultUtils.success(teamUserVOS);
    }
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> getIJoinedTeams(TeamQueryDTO teamQueryDTO, HttpServletRequest request){

        User loginUser = userService.getLoginUser(request);

        List<Long> userTeamRelationByUserId = userTeamService.getUserTeamRelationByUserId(loginUser.getId());
        teamQueryDTO.setIds(userTeamRelationByUserId);
        // 临时给予管理员权限。这么做是为了复用代码。因为非管理员，无法查看私密队伍。
        List<TeamUserVO> teamUserVOS = teamService.listTeams(teamQueryDTO, loginUser);

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
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.joinTeam(teamJoinRequest, loginUser);
        return res == true ? ResultUtils.success(true) : ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(true);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if (deleteRequest == null) throw new BusinessException(ErrorCode.NULL_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean b = teamService.deleteTeam(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }
}
