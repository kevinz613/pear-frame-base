package io.kevinz613.pear.convention.httpcode;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BaseHttpCode implements IHttpCode {

    SUCCESS("200", "成功"),
    ;

    private final String code;

    private final String message;


    /**
     * 响应码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 响应消息
     */
    @Override
    public String message() {
        return message;
    }
}
