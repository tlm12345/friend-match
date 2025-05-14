package com.yupi.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.yupi.usercenter.model.domain.User;
import lombok.Data;

import java.util.Date;

@Data
public class TeamUserVO {

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */

    private String name;

    /**
     * 描述
     */

    private String description;

    /**
     * 最大人数
     */

    private Integer maxNum;

    /**
     * 队伍已加入的人数
     */
    private Long hasJoinNum;
    /**
     * 当前查询的用户是否加入了该队伍
     */
    private Boolean hasJoin;

    /**
     * 过期时间
     */

    private Date expireTime;

    /**
     * 用户id
     */

    private Long userId;

    /**
     * 状态 0 - 公开， 1 - 私密， 2 - 加密
     */

    private Integer status;

    /**
     * 创建时间
     */

    private Date createTime;

    /**
     * 更新时间
     */

    private Date updateTime;

    /**
     * 创建该队伍的用户信息(脱敏)
     */
    private UserVO createUser;


}
