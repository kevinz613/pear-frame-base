package io.kevinz613.pear.mq.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.kevinz613.pear.mq.service.MessageSendService;
import io.kevinz613.pear.mq.model.Constants.MQConstant;
import io.kevinz613.pear.mq.model.base.TopicMessage;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 发送服务
 *
 * @author kevinz613
 */
@Component
@ConditionalOnProperty(name = "message.mq.type", havingValue = "rocketmq")
public class RocketMQMessageSendService implements MessageSendService {

    private final Logger logger = LoggerFactory.getLogger(RocketMQMessageSendService.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送消息
     *
     * @param message 消息
     * @return boolean
     */
    @Override
    public boolean send(TopicMessage message) {
        try {
            SendResult sendResult = rocketMQTemplate.syncSend(message.getDestination(), this.buildMessage(message));
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Exception e) {
            logger.error("RocketMQ消息:{} 发送失败", message);
            return false;
        }
    }

    /**
     * 发送事务消息
     *
     * @param message 事务消息
     * @param arg     其他参数
     * @return 事务发送结果
     */
    @Override
    public TransactionSendResult sendMessageInTransaction(TopicMessage message, Object arg) {
        return rocketMQTemplate.sendMessageInTransaction(message.getDestination(), this.buildMessage(message), arg);
    }

    /**
     * 构建RocketMQ发送的消息
     *
     * @param message 消息
     * @return message<字符串>
     */
    private Message<String> buildMessage(TopicMessage message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MQConstant.MESSAGE_KEY, message);
        return MessageBuilder.withPayload(jsonObject.toJSONString()).build();
    }
}
