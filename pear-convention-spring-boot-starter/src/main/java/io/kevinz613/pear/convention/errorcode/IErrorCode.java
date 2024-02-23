package io.kevinz613.pear.convention.errorcode;

/**
 * 平台错误码
 *
 * @author kevinz613
 */
public interface IErrorCode {

    /**
     * 错误码
     */
    String code();

    /**
     * 错误信息
     */
    String message();
}
