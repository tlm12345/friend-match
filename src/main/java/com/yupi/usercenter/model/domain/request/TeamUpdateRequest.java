package com.yupi.usercenter.model.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class TeamUpdateRequest {
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
     * 过期时间
     */

    private Date expireTime;


    /**
     * 状态 0 - 公开， 1 - 私密， 2 - 加密
     */

    private Integer status;

    /**
     * 密码
     */

    private String password;

}
