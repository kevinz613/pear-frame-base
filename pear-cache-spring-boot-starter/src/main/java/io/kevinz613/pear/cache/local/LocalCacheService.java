package io.kevinz613.pear.cache.local;

/**
 * 本地缓存服务
 *
 * @author kevinz613
 */
public interface LocalCacheService<K, V> {

    /**
     * 向本地缓存中添加数据
     *
     * @param key   缓存的key
     * @param value 缓存的value值
     */
    void put(K key, V value);

    /**
     * 如果key存在，则可从缓存中获取数据
     *
     * @param key 缓存的key
     * @return 缓存的值
     */
    V getIfPresent(K key);

    /**
     * 根据缓存的key移除缓存中的数据
     *
     * @param key 缓存的key
     */
    void remove(K key);

}
