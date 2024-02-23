package io.kevinz613.pear.cache.local.factory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 基于Guava的本地缓存工厂类
 *
 * @author kevinz613
 */
public class LocalGuavaCacheFactory {

    /**
     * 获取本地缓存-默认构造
     *
     * @return 缓存<k 、 v>
     */
    public static <K, V> Cache<K, V> getLocalCache() {
        return CacheBuilder.newBuilder().initialCapacity(200).concurrencyLevel(5).expireAfterWrite(300, TimeUnit.SECONDS).build();
    }

    /**
     * 获取本地缓存-自定义过期时间
     *
     * @param duration 过期时间
     * @return 缓存<k 、 v>
     */
    public static <K, V> Cache<K, V> getLocalCache(long duration) {
        return CacheBuilder.newBuilder().initialCapacity(200).concurrencyLevel(5).expireAfterWrite(duration, TimeUnit.SECONDS).build();
    }

    /**
     * 获取本地缓存-自定义容量和过期时间
     *
     * @param initialCapacity 初始容量
     * @param duration        过期时间
     * @return 缓存<k 、 v>
     */
    public static <K, V> Cache<K, V> getLocalCache(int initialCapacity, long duration) {
        return CacheBuilder.newBuilder().initialCapacity(initialCapacity).concurrencyLevel(5).expireAfterWrite(duration, TimeUnit.SECONDS).build();
    }
}
