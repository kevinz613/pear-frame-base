package io.kevinz613.pear.mq.model.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 主题消息
 *
 * @author kevinz613
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicMessage implements Serializable {

    /**
     * 消息目的地/消息的topic
     */
    private String destination;
}
