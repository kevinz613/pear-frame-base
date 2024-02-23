package io.kevinz613.pear.convention.exception;

import com.google.common.base.Strings;
import io.kevinz613.pear.convention.errorcode.IErrorCode;
import lombok.Getter;

import java.util.Optional;

/**
 * 抽象异常
 *
 * @author kevinz613
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(Strings.emptyToNull(message)).orElse(errorCode.message());
    }
}
