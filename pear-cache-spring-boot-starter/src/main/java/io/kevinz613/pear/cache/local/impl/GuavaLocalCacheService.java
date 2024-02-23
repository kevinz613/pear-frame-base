package io.kevinz613.pear.cache.local.impl;

import com.google.common.cache.Cache;
import io.kevinz613.pear.cache.local.LocalCacheService;
import io.kevinz613.pear.cache.local.factory.LocalGuavaCacheFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 基于Guava实现的本地缓存
 *
 * @author kevinz613
 */
@Component
@ConditionalOnProperty(name = "local.cache.type", havingValue = "guava")
public class GuavaLocalCacheService<K, V> implements LocalCacheService<K, V> {

    //获取本地缓存，基于guava实现
    private final Cache<K, V> cache = LocalGuavaCacheFactory.getLocalCache();

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
