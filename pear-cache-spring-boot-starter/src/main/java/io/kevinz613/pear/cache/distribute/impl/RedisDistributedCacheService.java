package io.kevinz613.pear.cache.distribute.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.kevinz613.pear.cache.distribute.DistributedCacheService;
import io.kevinz613.pear.cache.distribute.data.RedisData;
import io.kevinz613.pear.cache.lock.DistributedLockService;
import io.kevinz613.pear.cache.lock.factory.DistributedLockFactory;
import io.kevinz613.pear.cache.thread.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Redis 分布式缓存服务
 *
 * @author kevinz613
 */
@Component
@ConditionalOnProperty(name = "distribute.cache.type", havingValue = "redis")
public class RedisDistributedCacheService implements DistributedCacheService {

    private final Logger logger = LoggerFactory.getLogger(RedisDistributedCacheService.class);

    //缓存空数据的时长，单位秒
    private static final Long CACHE_NULL_TTL = 60L;
    //缓存的空数据
    private static final String EMPTY_VALUE = "";
    //缓存的空列表数据
    private static final String EMPTY_LIST_VALUE = "[]";
    //分布式锁key后缀
    private static final String LOCK_SUFFIX = "_lock";
    //线程休眠的毫秒数
    private static final long THREAD_SLEEP_MILLISECONDS = 50;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLockFactory distributedLockFactory;


    /**
     * 永久缓存数据
     *
     * @param key   缓存的key
     * @param value 缓存的value
     */
    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, this.getValue(value));
    }

    /**
     * 将数据缓存一段时间
     *
     * @param key     缓存的key
     * @param value   缓存的value
     * @param timeout 缓存时长
     * @param unit    缓存时长单位
     */
    @Override
    public void set(String key, Object value, Long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, this.getValue(value), timeout, unit);
    }

    /**
     * 设置缓存过期时间
     *
     * @param key     缓存的key
     * @param timeout 缓存时长
     * @param unit    缓存时长单位
     * @return 设置过期时间是否成功
     */
    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 保存缓存时设置逻辑过期时间
     *
     * @param key     缓存的key
     * @param value   缓存的value
     * @param timeout 缓存时长
     * @param unit    缓存时长单位
     */
    @Override
    public void setWithLogicalExpire(String key, Object value, Long timeout, TimeUnit unit) {
        RedisData redisData = new RedisData(value, LocalDateTime.now().plusSeconds(unit.toSeconds(timeout)));
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 获取缓存中的数据
     *
     * @param key 缓存的key
     * @return 缓存的value
     */
    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存数据
     *
     * @param key         缓存的key
     * @param targetClass 目标类
     * @return t
     */
    @Override
    public <T> T getObject(String key, Class<T> targetClass) {
        Object result = redisTemplate.opsForValue().get(key);
        if (Objects.isNull(result)) {
            return null;
        }
        try {
            return JSONUtil.toBean(result.toString(), targetClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据key列表批量获取value
     *
     * @param keys key列表
     * @return value集合
     */
    @Override
    public List<String> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 根据正则表达式获取所有的key集合
     *
     * @param pattern 正则表达式
     * @return key集合
     */
    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 删除指定的key
     *
     * @param key 缓存的key
     * @return 是否删除成功
     */
    @Override
    public Boolean delete(String key) {
        if (StrUtil.isEmpty(key)) {
            return false;
        }
        return redisTemplate.delete(key);
    }

    /**
     * 带参数查询对象和简单类型数据，防止缓存穿透
     *
     * @param keyPrefix  缓存的key前缀
     * @param id         缓存的业务标识
     * @param type       缓存的实际对象类型
     * @param dbFallback 查询数据库的Function函数
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return 业务数据
     */
    @Override
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix, id);
        //从redis中查询缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //缓存数据存在，直接返回
        if (StrUtil.isNotBlank(result)) {
            return this.getResult(result, type);
        }
        //缓存的数据是空字符串
        if (result.equals(EMPTY_VALUE)) {
            return null;
        }
        //缓存数据不存在，从数据库中查询数据
        R r = dbFallback.apply(id);
        //查询数据为空
        if (r == null) {
            redisTemplate.opsForValue().set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }
        //缓存数据
        this.set(key, r, timeout, unit);
        return r;
    }

    /**
     * 不带参数查询对象和简单类型数据，防止缓存穿透
     *
     * @param keyPrefix  缓存的key前缀
     * @param type       缓存实际对象类型
     * @param dbFallback 无参数查询数据库数据
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R> R queryWithPassThroughWithoutArgs(String keyPrefix, Class<R> type, Supplier<R> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix);
        //从redis中查询缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //缓存数据存在，直接返回
        if (StrUtil.isNotBlank(result)) {
            return this.getResult(result, type);
        }
        //缓存的数据是空字符串
        if (result.equals(EMPTY_VALUE)) {
            return null;
        }
        //缓存数据不存在，从数据库中查询数据
        R r = dbFallback.get();
        //查询数据为空
        if (r == null) {
            redisTemplate.opsForValue().set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }
        //缓存数据
        this.set(key, r, timeout, unit);
        return r;
    }

    /**
     * 带参数查询集合数据，防止缓存穿透
     *
     * @param keyPrefix  缓存的key前缀
     * @param id         缓存的业务标识
     * @param type       缓存的实际对象类型
     * @param dbFallback 查询数据库的Function函数
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return 列表<r>
     */
    @Override
    public <R, ID> List<R> queryWithPassThroughList(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix);
        //从redis中查询缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //缓存数据存在，直接返回
        if (StrUtil.isNotBlank(result)) {
            return this.getResultList(result, type);
        }
        //缓存的数据是空字符串
        if (result.equals(EMPTY_VALUE)) {
            return null;
        }
        //缓存数据不存在，从数据库中查询数据
        List<R> r = dbFallback.apply(id);
        //查询数据为空
        if (r == null || r.isEmpty()) {
            redisTemplate.opsForValue().set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }
        //缓存数据
        this.set(key, r, timeout, unit);
        return r;
    }

    /**
     * 不带参数查询集合数据，防止缓存穿透
     *
     * @param keyPrefix  缓存的key前缀
     * @param type       缓存实际对象类型
     * @param dbFallback 无参数查询数据库数据
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R> List<R> queryWithPassThroughListWithoutArgs(String keyPrefix, Class<R> type, Supplier<List<R>> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix);
        //从redis中查询缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //缓存数据存在，直接返回
        if (StrUtil.isNotBlank(result)) {
            return this.getResultList(result, type);
        }
        //缓存的数据是空字符串
        if (result.equals(EMPTY_VALUE)) {
            return null;
        }
        //缓存数据不存在，从数据库中查询数据
        List<R> r = dbFallback.get();
        //查询数据为空
        if (r == null || r.isEmpty()) {
            redisTemplate.opsForValue().set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }
        //缓存数据
        this.set(key, r, timeout, unit);
        return r;
    }

    /**
     * 带参数查询数据，按照逻辑过期时间读取缓存数据，新开线程重建缓存，其他线程直接返回逻辑过期数据，不占用资源
     *
     * @param keyPrefix  缓存的key前缀
     * @param id         缓存的业务标识
     * @param type       缓存的实际对象类型
     * @param dbFallback 查询数据库的Function函数
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix, id);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //判断数据是否存在
        if (StrUtil.isBlank(result)) {
            try {
                //构建缓存数据
                buildCache(id, dbFallback, timeout, unit, key);
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                //重试
                return queryWithLogicalExpire(keyPrefix, id, type, dbFallback, timeout, unit);
            } catch (InterruptedException e) {
                logger.error("query data with logical expire | {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        //命中，需要先把json反序列化为对象
        RedisData redisData = this.getResult(result, RedisData.class);
        if (EMPTY_VALUE.equals(redisData.getData())) {
            return null;
        }
        R r = this.getResult(redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //未过期，直接返回数据
            return r;
        }
        //缓存获取，构建缓存数据
        buildCache(id, dbFallback, timeout, unit, key);
        //返回逻辑过期数据
        return r;
    }

    /**
     * 构建缓存逻辑过期数据
     *
     * @param id         编号
     * @param dbFallback 数据库回退
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @param key        缓存的key
     */
    private <ID, R> void buildCache(ID id, Function<ID, R> dbFallback, Long timeout, TimeUnit unit, String key) {
        //分布式锁
        String lockKey = this.getLockKey(key);
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        ThreadPoolUtils.execute(() -> {
            try {
                boolean isLock = distributedLock.tryLock();
                //获取锁成功，Double check
                if (isLock) {
                    R newR = null;
                    //从redis中获取缓存数据
                    String result = redisTemplate.opsForValue().get(key);
                    if (StrUtil.isEmpty(result)) {
                        //查询数据库
                        newR = dbFallback.apply(id);
                    } else {
                        //命中 需要先把json反序列化为对象
                        RedisData redisData = this.getResult(result, RedisData.class);
                        LocalDateTime expireTime = redisData.getExpireTime();
                        if (expireTime.isBefore(LocalDateTime.now())) {
                            //查询数据库
                            newR = dbFallback.apply(id);
                        }
                    }
                    if (newR != null) {
                        //重建缓存
                        this.setWithLogicalExpire(key, newR, timeout, unit);
                    } else {
                        this.setWithLogicalExpire(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("build cache | {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                distributedLock.unlock();
            }
        });

    }

    /**
     * 获取锁缓存的key
     *
     * @param key 缓存的key
     * @return 字符串
     */
    private String getLockKey(String key) {
        return key.concat(LOCK_SUFFIX);
    }

    /**
     * 不带参数查询数据，按照逻辑过期时间读取缓存数据，新开线程重建缓存，其他线程直接返回逻辑过期数据，不占用资源
     *
     * @param keyPrefix  缓存的key前缀
     * @param type       缓存实际对象类型
     * @param dbFallback 无参数查询数据库数据
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R> R queryWithLogicalExpireWithoutArgs(String keyPrefix, Class<R> type, Supplier<R> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //判断数据是否存在
        if (StrUtil.isBlank(result)) {
            try {
                //构建缓存数据
                buildCacheWithoutArgs(dbFallback, timeout, unit, key);
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                //重试
                return queryWithLogicalExpireWithoutArgs(keyPrefix, type, dbFallback, timeout, unit);
            } catch (InterruptedException e) {
                logger.error("query data with logical expire | {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        //命中，需要先把json反序列化为对象
        RedisData redisData = this.getResult(result, RedisData.class);
        if (EMPTY_VALUE.equals(redisData.getData())) {
            return null;
        }
        R r = this.getResult(redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //未过期，直接返回数据
            return r;
        }
        //缓存获取，构建缓存数据
        buildCacheWithoutArgs(dbFallback, timeout, unit, key);
        //返回逻辑过期数据
        return r;
    }

    /**
     * 在没有参数情况下构建缓存逻辑过期数据
     *
     * @param dbFallback 数据库回退
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @param key        缓存的key
     */
    private <R> void buildCacheWithoutArgs(Supplier<R> dbFallback, Long timeout, TimeUnit unit, String key) {
        //分布式锁
        String lockKey = this.getLockKey(key);
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        ThreadPoolUtils.execute(() -> {
            try {
                boolean isLock = distributedLock.tryLock();
                //获取锁成功，Double check
                if (isLock) {
                    R newR = null;
                    //从redis中获取缓存数据
                    String result = redisTemplate.opsForValue().get(key);
                    if (StrUtil.isEmpty(result)) {
                        //查询数据库
                        newR = dbFallback.get();
                    } else {
                        //命中 需要先把json反序列化为对象
                        RedisData redisData = this.getResult(result, RedisData.class);
                        LocalDateTime expireTime = redisData.getExpireTime();
                        if (expireTime.isBefore(LocalDateTime.now())) {
                            //查询数据库
                            newR = dbFallback.get();
                        }
                    }
                    if (newR != null) {
                        //重建缓存
                        this.setWithLogicalExpire(key, newR, timeout, unit);
                    } else {
                        this.setWithLogicalExpire(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("build cache without args| {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                distributedLock.unlock();
            }
        });

    }

    /**
     * 带参数查询集合数据，按照逻辑过期时间读取缓存数据，新开线程重建缓存，其他线程直接返回逻辑过期数据，不占用资源
     *
     * @param keyPrefix  缓存的key前缀
     * @param id         缓存的业务标识
     * @param type       缓存的实际对象类型
     * @param dbFallback 查询数据库的Function函数
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R, ID> List<R> queryWithLogicalExpireList(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix, id);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //判断数据是否存在
        if (StrUtil.isBlank(result)) {
            try {
                //构建缓存数据
                buildCacheList(id, dbFallback, timeout, unit, key);
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                //重试
                return queryWithLogicalExpireList(keyPrefix, id, type, dbFallback, timeout, unit);
            } catch (InterruptedException e) {
                logger.error("query data with logical expire | {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        //命中，需要先把json反序列化为对象
        RedisData redisData = this.getResult(result, RedisData.class);
        if (EMPTY_LIST_VALUE.equals(redisData.getData())) {
            return null;
        }
        List<R> list = this.getResultList(JSONUtil.toJsonStr(redisData.getData()), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //未过期，直接返回数据
            return list;
        }
        //缓存获取，构建缓存数据
        buildCacheList(id, dbFallback, timeout, unit, key);
        //返回逻辑过期数据
        return list;
    }

    private <ID, R> void buildCacheList(ID id, Function<ID, List<R>> dbFallback, Long timeout, TimeUnit unit, String key) {
        //分布式锁
        String lockKey = this.getLockKey(key);
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        ThreadPoolUtils.execute(() -> {
            try {
                boolean isLock = distributedLock.tryLock();
                //获取锁成功，Double check
                if (isLock) {
                    List<R> newR = null;
                    //从redis中获取缓存数据
                    String result = redisTemplate.opsForValue().get(key);
                    if (StrUtil.isEmpty(result)) {
                        //查询数据库
                        newR = dbFallback.apply(id);
                    } else {
                        //命中 需要先把json反序列化为对象
                        RedisData redisData = this.getResult(result, RedisData.class);
                        LocalDateTime expireTime = redisData.getExpireTime();
                        if (expireTime.isBefore(LocalDateTime.now())) {
                            //查询数据库
                            newR = dbFallback.apply(id);
                        }
                    }
                    if (newR != null) {
                        //重建缓存
                        this.setWithLogicalExpire(key, newR, timeout, unit);
                    } else {
                        this.setWithLogicalExpire(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("build cache list | {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                distributedLock.unlock();
            }
        });
    }

    /**
     * 不带参数查询集合数据，按照逻辑过期时间读取缓存数据，新开线程重建缓存，其他线程直接返回逻辑过期数据，不占用资源
     *
     * @param keyPrefix  缓存的key前缀
     * @param type       缓存实际对象类型
     * @param dbFallback 无参数查询数据库数据
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R> List<R> queryWithLogicalExpireListWithoutArgs(String keyPrefix, Class<R> type, Supplier<List<R>> dbFallback, Long timeout, TimeUnit unit) {
        //获取存储到redis中的数据key
        String key = this.getKey(keyPrefix);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        //判断数据是否存在
        if (StrUtil.isBlank(result)) {
            try {
                //构建缓存数据
                buildCacheListWithoutArgs(dbFallback, timeout, unit, key);
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                //重试
                return queryWithLogicalExpireListWithoutArgs(keyPrefix, type, dbFallback, timeout, unit);
            } catch (InterruptedException e) {
                logger.error("query data with logical expire | {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        //命中，需要先把json反序列化为对象
        RedisData redisData = this.getResult(result, RedisData.class);
        if (EMPTY_LIST_VALUE.equals(redisData.getData())) {
            return new ArrayList<>();
        }
        List<R> list = this.getResultList(JSONUtil.toJsonStr(redisData.getData()), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //未过期，直接返回数据
            return list;
        }
        //缓存获取，构建缓存数据
        buildCacheListWithoutArgs(dbFallback, timeout, unit, key);
        //返回逻辑过期数据
        return list;
    }

    private <R> void buildCacheListWithoutArgs(Supplier<List<R>> dbFallback, Long timeout, TimeUnit unit, String key) {
        //分布式锁
        String lockKey = this.getLockKey(key);
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        ThreadPoolUtils.execute(() -> {
            try {
                boolean isLock = distributedLock.tryLock();
                //获取锁成功，Double check
                if (isLock) {
                    List<R> newR = null;
                    //从redis中获取缓存数据
                    String result = redisTemplate.opsForValue().get(key);
                    if (StrUtil.isEmpty(result)) {
                        //查询数据库
                        newR = dbFallback.get();
                    } else {
                        //命中 需要先把json反序列化为对象
                        RedisData redisData = this.getResult(result, RedisData.class);
                        LocalDateTime expireTime = redisData.getExpireTime();
                        if (expireTime.isBefore(LocalDateTime.now())) {
                            //查询数据库
                            newR = dbFallback.get();
                        }
                    }
                    if (newR != null) {
                        //重建缓存
                        this.setWithLogicalExpire(key, newR, timeout, unit);
                    } else {
                        this.setWithLogicalExpire(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("build cache list | {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                distributedLock.unlock();
            }
        });
    }

    /**
     * 带参数查询数据，按照互斥锁方式获取缓存数据，同一时刻只有一个线程访问数据库，其他线程访问不到数据重试
     *
     * @param keyPrefix  缓存的key前缀
     * @param id         缓存的业务标识
     * @param type       缓存的实际对象类型
     * @param dbFallback 查询数据库的Function函数
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long timeout, TimeUnit unit) {
        //获取缓存中的key
        String key = this.getKey(keyPrefix, id);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(result)) {
            //存在数据，直接返回
            return this.getResult(result, type);
        }
        //缓存了空字符串
        if (EMPTY_VALUE.equals(result)) {
            return null;
        }
        String lockKey = this.getLockKey(key);
        R r = null;
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        try {
            boolean isLock = distributedLock.tryLock();
            //获取分布式锁失败，重试
            if (!isLock) {
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                return queryWithMutex(keyPrefix, id, type, dbFallback, timeout, unit);
            }
            //获取锁成功，Double check
            String str = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(str)) {
                return this.getResult(str, type);
            }
            //成功获取到锁
            r = dbFallback.apply(id);
            //数据库本身不存在数据
            if (r == null) {
                //缓存空数据
                this.set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                return null;
            }
            //数据库存在数据
            this.set(key, r, timeout, unit);

        } catch (InterruptedException e) {
            logger.error("query data with mutex |{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            distributedLock.unlock();
        }
        return r;
    }

    /**
     * 不带参数查询数据，按照互斥锁方式获取缓存数据，同一时刻只有一个线程访问数据库，其他线程访问不到数据重试
     *
     * @param keyPrefix  缓存的key前缀
     * @param type       缓存实际对象类型
     * @param dbFallback 无参数查询数据库数据
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R> R queryWithMutexWithoutArgs(String keyPrefix, Class<R> type, Supplier<R> dbFallback, Long timeout, TimeUnit unit) {
        //获取缓存中的key
        String key = this.getKey(keyPrefix);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(result)) {
            //存在数据，直接返回
            return this.getResult(result, type);
        }
        //缓存了空字符串
        if (EMPTY_VALUE.equals(result)) {
            return null;
        }
        String lockKey = this.getLockKey(key);
        R r = null;
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        try {
            boolean isLock = distributedLock.tryLock();
            //获取分布式锁失败，重试
            if (!isLock) {
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                return queryWithMutexWithoutArgs(keyPrefix, type, dbFallback, timeout, unit);
            }
            //获取锁成功，Double check
            String str = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(str)) {
                return this.getResult(str, type);
            }
            //成功获取到锁
            r = dbFallback.get();
            //数据库本身不存在数据
            if (r == null) {
                //缓存空数据
                this.set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                return null;
            }
            //数据库存在数据
            this.set(key, r, timeout, unit);

        } catch (InterruptedException e) {
            logger.error("query data with mutex |{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            distributedLock.unlock();
        }
        return r;
    }

    /**
     * 带参数查询数据，按照互斥锁方式获取缓存数据，同一时刻只有一个线程访问数据库，其他线程访问不到数据重试
     *
     * @param keyPrefix  缓存的key前缀
     * @param id         缓存的业务标识
     * @param type       缓存的实际对象类型
     * @param dbFallback 查询数据库的Function函数
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R, ID> List<R> queryWithMutexList(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long timeout, TimeUnit unit) {
        //获取缓存中的key
        String key = this.getKey(keyPrefix, id);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(result)) {
            //存在数据，直接返回
            return this.getResultList(result, type);
        }
        //缓存了空字符串
        if (EMPTY_VALUE.equals(result)) {
            return null;
        }
        String lockKey = this.getLockKey(key);
        List<R> r = null;
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        try {
            boolean isLock = distributedLock.tryLock();
            //获取分布式锁失败，重试
            if (!isLock) {
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                return queryWithMutexList(keyPrefix, id, type, dbFallback, timeout, unit);
            }
            //获取锁成功，Double check
            String str = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(str)) {
                return this.getResultList(str, type);
            }
            //成功获取到锁
            r = dbFallback.apply(id);
            //数据库本身不存在数据
            if (r == null) {
                //缓存空数据
                this.set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                return null;
            }
            //数据库存在数据
            this.set(key, r, timeout, unit);

        } catch (InterruptedException e) {
            logger.error("query data with mutex list |{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            distributedLock.unlock();
        }
        return r;
    }

    /**
     * 不带参数查询数据，按照互斥锁方式获取缓存数据，同一时刻只有一个线程访问数据库，其他线程访问不到数据重试
     *
     * @param keyPrefix  缓存的key前缀
     * @param type       缓存实际对象类型
     * @param dbFallback 无参数查询数据库数据
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @return r
     */
    @Override
    public <R> List<R> queryWithMutexListWithoutArgs(String keyPrefix, Class<R> type, Supplier<List<R>> dbFallback, Long timeout, TimeUnit unit) {
        //获取缓存中的key
        String key = this.getKey(keyPrefix);
        //从redis中获取缓存数据
        String result = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(result)) {
            //存在数据，直接返回
            return this.getResultList(result, type);
        }
        //缓存了空字符串
        if (EMPTY_VALUE.equals(result)) {
            return null;
        }
        String lockKey = this.getLockKey(key);
        List<R> r = null;
        //获取分布式锁
        DistributedLockService distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        try {
            boolean isLock = distributedLock.tryLock();
            //获取分布式锁失败，重试
            if (!isLock) {
                Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                return queryWithMutexListWithoutArgs(keyPrefix, type, dbFallback, timeout, unit);
            }
            //获取锁成功，Double check
            String str = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(str)) {
                return this.getResultList(str, type);
            }
            //成功获取到锁
            r = dbFallback.get();
            //数据库本身不存在数据
            if (r == null) {
                //缓存空数据
                this.set(key, EMPTY_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                return null;
            }
            //数据库存在数据
            this.set(key, r, timeout, unit);

        } catch (InterruptedException e) {
            logger.error("query data with mutex list |{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            distributedLock.unlock();
        }
        return r;
    }
}
