package io.kevinz613.pear.mq.service;

import io.kevinz613.pear.mq.model.base.TopicMessage;
import org.apache.rocketmq.client.producer.TransactionSendResult;

/**
 * 消息发送服务
 *
 * @author kevinz613
 */
public interface MessageSendService {

    /**
     * 发送消息
     *
     * @param message 消息
     * @return boolean
     */
    boolean send(TopicMessage message);

    /**
     * 发送事务消息
     *
     * @param message 事务消息
     * @param arg     其他参数
     * @return 事务发送结果
     */
    default TransactionSendResult sendMessageInTransaction(TopicMessage message, Object arg){
        return null;
    }
}
