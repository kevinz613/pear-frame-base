package io.kevinz613.pear.cache.lock.impl;

import io.kevinz613.pear.cache.lock.DistributedLockService;
import io.kevinz613.pear.cache.lock.factory.DistributedLockFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redisson的分布式锁实现
 *
 * @author kevinz613
 */
@Component
@ConditionalOnProperty(name = "distribute.lock.type", havingValue = "redisson")
public class RedissonLockFactory implements DistributedLockFactory {

    private final Logger logger = LoggerFactory.getLogger(RedissonLockFactory.class);

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取分布式锁
     *
     * @param key 缓存的key
     * @return 分布式锁服务接口
     */
    @Override
    public DistributedLockService getDistributedLock(String key) {
        RLock lock = redissonClient.getLock(key);
        return new DistributedLockService() {
            @Override
            public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                boolean isLock = lock.tryLock(waitTime, leaseTime, unit);
                logger.info("{} get lock result: {}", key, isLock);
                return isLock;
            }

            @Override
            public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
                return lock.tryLock(waitTime, unit);
            }

            @Override
            public boolean tryLock() throws InterruptedException {
                return lock.tryLock();
            }

            @Override
            public void lock(long leaseTime, TimeUnit unit) {
                lock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                if (isLocked() && isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

            @Override
            public boolean isLocked() {
                return lock.isLocked();
            }

            @Override
            public boolean isHeldByThread(long threadId) {
                return lock.isHeldByThread(threadId);
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return lock.isHeldByCurrentThread();
            }
        };
    }
}
