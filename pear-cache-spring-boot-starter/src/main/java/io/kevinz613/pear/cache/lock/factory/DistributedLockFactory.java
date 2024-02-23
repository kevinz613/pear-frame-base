package io.kevinz613.pear.cache.lock.factory;


import io.kevinz613.pear.cache.lock.DistributedLockService;

/**
 * 分布式锁工厂
 *
 * @author kevinz613
 */
public interface DistributedLockFactory {

    /**
     * 获取分布式锁
     *
     * @param key 缓存的key
     * @return 分布式锁服务接口
     */
    DistributedLockService getDistributedLock(String key);
}
