package com.yupi.usercenter.model.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class TeamJoinRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 密码
     */
    private String password;
}
