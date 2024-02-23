package io.kevinz613.pear.cache.model.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用缓存模型
 *
 * @author kevinz613
 */

@Data
public class CommonCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 2596349048955820766L;

    //缓存数据是否存在
    protected boolean exist;

    //缓存版本号
    protected Long version;

    //稍后重试
    protected boolean retryLater;

}
