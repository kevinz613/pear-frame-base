package io.kevinz613.pear.mq.event.impl;

import com.alibaba.fastjson.JSON;
import io.kevinz613.pear.mq.event.EventPublishService;
import io.kevinz613.pear.mq.model.base.BaseEvent;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 事件发布服务
 *
 * @author kevinz613
 */
@Component
@ConditionalOnProperty(name = "message.mq.event.type", havingValue = "rocketmq")
public class RocketMQEventPublishService implements EventPublishService {

    private final Logger logger = LoggerFactory.getLogger(RocketMQEventPublishService.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发布事件
     *
     * @param topic   主题
     * @param message 消息
     * @return boolean
     */
    @Override
    public boolean publish(String topic, BaseEvent<?> message) {
        try {
            String mqMessage = JSON.toJSONString(message);
            logger.info("发送MQ消息,topic:{},message:{}", topic, mqMessage);
            SendResult sendResult = rocketMQTemplate.syncSend(topic, mqMessage);
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Exception e) {
            logger.info("发送MQ消息失败,topic:{},message:{}", topic, JSON.toJSONString(message), e);
            //大部分MQ消息发送失败需要任务补偿
            return false;
        }
    }

    /**
     * 发布延迟
     *
     * @param topic          主题
     * @param message        消息
     * @param delayTimeLevel 延迟时间级别
     * @return boolean
     */
    @Override
    public boolean publishDelay(String topic, BaseEvent<?> message, int delayTimeLevel) {
        try {
            String mqMessage = JSON.toJSONString(message);
            logger.info("发送MQ延迟消息,topic:{},message:{}", topic, mqMessage);
            SendResult sendResult = rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(message).build(), 1000, delayTimeLevel);
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Exception e) {
            logger.info("发送MQ延迟消息失败,topic:{},message:{}", topic, JSON.toJSONString(message), e);
            //大部分MQ消息发送失败需要任务补偿
            return false;
        }
    }

}
