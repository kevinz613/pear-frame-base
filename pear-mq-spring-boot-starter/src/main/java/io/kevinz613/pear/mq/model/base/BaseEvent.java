package io.kevinz613.pear.mq.model.base;

import java.util.Date;

/**
 * 基本事件
 *
 * @author kevinz613
 */
public class BaseEvent<T> {

    private String uuid;

    private Date timestamp;

    private T data;
}
