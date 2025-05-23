package com.yupi.usercenter.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = -7730626539467247246L;

    protected int pageSize = 10;

    protected int pageNum = 1;

}
