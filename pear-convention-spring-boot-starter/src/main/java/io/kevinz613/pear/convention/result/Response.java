package io.kevinz613.pear.convention.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用响应
 *
 * @author kevinz613
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -7426404123994975937L;

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;
}
