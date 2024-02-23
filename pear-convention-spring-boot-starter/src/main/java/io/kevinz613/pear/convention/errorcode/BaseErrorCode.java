package io.kevinz613.pear.convention.errorcode;

import lombok.AllArgsConstructor;

/**
 * 基本错误码
 *
 * @author kevinz613
 */
@AllArgsConstructor
public enum BaseErrorCode implements IErrorCode {

    /******************** 一级宏观错误码 客户端错误 ********************/
    CLIENT_ERROR("A0001", "用户端错误"),

    /******************** 二级宏观错误码 用户注册登录 ********************/
    USER_REGISTER_ERROR("A0100", "用户注册错误"),
    USER_NAME_VERIFY_ERROR("A0101", "用户名校验失败"),
    USER_NAME_EXIST_ERROR("A0102", "用户名已存在"),
    PASSWORD_VERIFY_ERROR("A0103", "密码校验失败"),
    TOKEN_NULL_ERROR("A0201", "Token为空"),
    TOKEN_NOT_AVAILABLE_ERROR("A0202", "Token已使用或过期"),


    /******************** 一级宏观错误码  系统错误 ********************/
    SERVICE_ERROR("B0001", "系统执行出错"),

    /******************** 二级宏观错误码 系统执行超时  ********************/
    SERVICE_TIMEOUT_ERROR("B0100", "系统执行超时"),


    /******************** 一级宏观错误码 调用第三方服务出错 ********************/
    REMOTE_ERROR("C0001", "调用第三方服务出错"),

    /******************** 二级宏观错误码 调用第三方服务超时  ********************/
    REMOTE_TIMEOUT_ERROR("C0100", "调用第三方服务超时");

    private final String code;

    private final String message;


    /**
     * 错误码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 错误信息
     */
    @Override
    public String message() {
        return message;
    }
}
