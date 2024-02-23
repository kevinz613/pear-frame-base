package io.kevinz613.pear.idgenerator;

/**
 * id 生成服务
 *
 * @author kevinz613
 */
public interface IdGenerateService {

    /**
     * 生成 ID
     *
     * @return long
     */
    long generateId();

    /**
     * 生成 字符串类型ID
     *
     * @return 字符串
     */
    String generateIdStr();
}
