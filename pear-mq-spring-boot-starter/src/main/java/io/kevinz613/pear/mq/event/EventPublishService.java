package io.kevinz613.pear.mq.event;

import io.kevinz613.pear.mq.model.base.BaseEvent;

/**
 * 事件发送服务
 *
 * @author kevinz613
 */
public interface EventPublishService {

    /**
     * 发布事件
     *
     * @param topic   主题
     * @param message 消息
     * @return boolean
     */
    boolean publish(String topic, BaseEvent<?> message);
    

    /**
     * 发布延迟
     *
     * @param topic          主题
     * @param message        消息
     * @param delayTimeLevel 延迟时间级别
     * @return boolean
     */
    boolean publishDelay(String topic,BaseEvent<?> message, int delayTimeLevel);
}
