package com.yupi.usercenter.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.yupi.usercenter.common.PageQuery;
import lombok.Data;

import java.util.Date;

@Data
public class TeamQueryDTO extends PageQuery {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 最大人数
     */
    @TableField(value = "maxNum")
    private Integer maxNum;

    /**
     * 用户id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 状态 0 - 公开， 1 - 私密， 2 - 加密
     */
    @TableField(value = "status")
    private Integer status;
}
