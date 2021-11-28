package com.feng.chat.common.constants;

/**
 * @Description 消息返回码
 * @Author fengsy
 * @Date 9/29/21
 */
public enum ResultCodeEnum {
    SUCCESS(0, "Success"), // 成功
    AUTH_FAILED(1, "登录失败"),
    NO_TOKEN(2, "没有授权码"),
    UNKNOW_ERROR(3, "未知错误");

    private Integer code;
    private String desc;

    ResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
