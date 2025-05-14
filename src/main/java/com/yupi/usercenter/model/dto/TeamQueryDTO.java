package com.yupi.usercenter.model.dto;

import com.yupi.usercenter.common.PageQuery;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamQueryDTO extends PageQuery {
    /**
     * id
     */
    private Long id;

    /**
     * 批量查询时传入id列表
     */
    private List<Long> ids;

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
     * 用户id
     */
    private Long userId;

    /**
     * 状态 0 - 公开， 1 - 私密， 2 - 加密
     */
    private Integer status;

    /**
     * 队伍过期时间
     */
    private Date expireTime;

    /**
     * 查找关键字，用于管理员对队伍名称和队伍描述同时进行查找
     */
    private String searchText;
}
