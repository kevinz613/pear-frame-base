package io.kevinz613.pear.idgenerator.impl;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import io.kevinz613.pear.idgenerator.IdGenerateService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Yitter ID 生成服务
 *
 * @author kevinz613
 */
@Component
public class YitterIdGenerateService implements IdGenerateService {

    @PostConstruct
    public void init() {
        // 创建 IdGeneratorOptions 对象，可在构造函数中输入 WorkerId：
        IdGeneratorOptions options = new IdGeneratorOptions(getRandom(0,31));
        options.WorkerIdBitLength = 10; // 默认值6，限定 WorkerId 最大值为2^6-1，即默认最多支持64个节点。
        options.SeqBitLength = 6; // 默认值6，限制每毫秒生成的ID个数。若生成速度超过5万个/秒，建议加大 SeqBitLength 到 10。
        // 保存参数（务必调用，否则参数设置不生效）：
        YitIdHelper.setIdGenerator(options);
    }


    @Override
    public long generateId() {
        return YitIdHelper.nextId();
    }

    @Override
    public String generateIdStr() {
        return Long.toString(YitIdHelper.nextId());
    }

    /**
     * 随机获取workId
     *
     * @param start 开始
     * @param end   结束
     * @return short
     */
    private static short getRandom(int start, int end) {
        short randomWorkId = (short) (Math.random() * (end - start + 1) + start);
        return randomWorkId;
    }
}
