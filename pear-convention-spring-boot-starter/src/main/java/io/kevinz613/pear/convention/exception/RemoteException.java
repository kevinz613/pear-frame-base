package io.kevinz613.pear.convention.exception;

import io.kevinz613.pear.convention.errorcode.BaseErrorCode;
import io.kevinz613.pear.convention.errorcode.IErrorCode;

/**
 * 远程调用异常异常
 *
 * @author kevinz613
 */
public class RemoteException extends AbstractException {

    public RemoteException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public RemoteException(String message) {
        this(message, null, BaseErrorCode.REMOTE_ERROR);
    }

    public RemoteException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public RemoteException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }
}
