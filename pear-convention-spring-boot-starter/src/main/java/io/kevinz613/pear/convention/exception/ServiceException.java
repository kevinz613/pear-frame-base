package io.kevinz613.pear.convention.exception;

import io.kevinz613.pear.convention.errorcode.BaseErrorCode;
import io.kevinz613.pear.convention.errorcode.IErrorCode;

/**
 * 服务端异常
 *
 * @author kevinz613
 */
public class ServiceException extends AbstractException {

    public ServiceException(String message) {
        this(message, null, BaseErrorCode.SERVICE_ERROR);
    }

    public ServiceException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ServiceException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ServiceException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }
}
