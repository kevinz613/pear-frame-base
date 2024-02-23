package io.kevinz613.pear.cache.local.impl;

import com.github.benmanes.caffeine.cache.Cache;
import io.kevinz613.pear.cache.local.LocalCacheService;
import io.kevinz613.pear.cache.local.factory.LocalCaffeineCacheFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 基于Caffeine实现的本地缓存，需要在启动类加EnableCaching
 *
 * @author kevinz613
 */
@Component
@ConditionalOnProperty(name = "local.cache.type", havingValue = "caffeine")
public class CaffeineLocalCacheService<K, V> implements LocalCacheService<K, V> {

    //获取本地缓存，基于Caffeine实现
    private final Cache<K, V> cache = LocalCaffeineCacheFactory.getLocalCache();

    /**
     * 向本地缓存中添加数据
     *
     * @param key   缓存的key
     * @param value 缓存的value值
     */
    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * 如果key存在，则可从缓存中获取数据
     *
     * @param key 缓存的key
     * @return 缓存的值
     */
    @Override
    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }

    /**
     * 根据缓存的key移除缓存中的数据
     *
     * @param key 缓存的key
     */
    @Override
    public void remove(K key) {
        cache.invalidate(key);
    }
}
