package io.kevinz613.pear.cache.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务接口
 *
 * @author kevinz613
 */
public interface DistributedLockService {

    /**
     * 尝试获取锁
     *
     * @param waitTime  等待时间
     * @param leaseTime 租赁时间
     * @param unit      缓存时长单位
     * @return boolean
     * @throws InterruptedException 打断异常
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException;

    boolean tryLock() throws InterruptedException;

    /**
     * 锁
     *
     * @param leaseTime 租赁时间
     * @param unit      缓存时长单位
     */
    void lock(long leaseTime, TimeUnit unit);

    /**
     * 开锁
     */
    void unlock();

    /**
     * 判断是否加锁
     *
     * @return boolean
     */
    boolean isLocked();

    /**
     * 判断是否由线程保持
     *
     * @param threadId 线程 ID
     * @return boolean
     */
    boolean isHeldByThread(long threadId);

    /**
     * 是否由当前线程持有
     *
     * @return boolean
     */
    boolean isHeldByCurrentThread();

}
