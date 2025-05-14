package com.yupi.usercenter.model.enums;

/**
 * 队伍状态枚举类
 * @author tlm
 */
public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私密"),
    SECRETE(2, "加密");


    private Integer status;
    private String text;

    public static TeamStatusEnum getTeamStatusByValue(Integer v){
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum value : values) {
            Integer s = value.getStatus();
            if (s.equals(v)){
                return value;
            }
        }

        return null;
    }

    TeamStatusEnum(int status, String text){
        this.status = status;
        this.text = text;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
