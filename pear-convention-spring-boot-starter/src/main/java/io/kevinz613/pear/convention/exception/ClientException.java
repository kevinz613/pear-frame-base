package io.kevinz613.pear.convention.exception;

import io.kevinz613.pear.convention.errorcode.BaseErrorCode;
import io.kevinz613.pear.convention.errorcode.IErrorCode;

/**
 * 客户端异常
 *
 * @author kevinz613
 */
public class ClientException extends AbstractException {

    public ClientException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ClientException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

}
