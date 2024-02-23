package io.kevinz613.pear.convention.httpcode;

/**
 * Http 响应码
 *
 * @author kevinz613
 */
public interface IHttpCode {

    /**
     * 响应码
     */
    String code();

    /**
     * 响应消息
     */
    String message();
}
