package io.kevinz613.pear.cache.distribute;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import io.kevinz613.pear.cache.distribute.conversion.TypeConversion;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 分布式缓存服务接口，在满足分布式缓存的需求基础上，解决缓存击穿、穿透和雪崩问题
 *
 * @author kevinz613
 */
public interface DistributedCacheService {


    /**
     * 永久缓存数据
     *
     * @param key   缓存的key
     * @param value 缓存的value
     */
    void set(String key, Object value);

    /**
     * 将数据缓存一段时间
     *
     * @param key     缓存的key
     * @param value   缓存的value
     * @param timeout 缓存时长
     * @param unit    缓存时长单位
     */
    void set(String key, Object value, Long timeout, TimeUnit unit);


    /**
     * 设置缓存过期时间
     *
     * @param key     缓存的key
     * @param timeout 缓存时长
     * @param unit    缓存时长单位
     * @return 设置过期时间是否成功
     */
    Boolean expire(String key, final long timeout, final TimeUnit unit);

    /**
     * 保存缓存时设置逻辑过期时间
     *
     * @param key     缓存的key
     * @param value   缓存的value
     * @param timeout 缓存时长
     * @param unit    缓存时长单位
     */
    void setWithLogicalExpire(String key, Object value, Long timeout, TimeUnit unit);

    /**
     * 获取缓存中的数据
     *
     * @param key 缓存的key
     * @return 缓存的value
     */
    String get(String key);

    /**
     * 获取缓存数据
     *
     * @param key         缓存的key
     * @param targetClass 目标类
     * @return t
     */
    <T> T getObject(String key, Class<T> targetClass);

    /**
     * 根据key列表批量获取value
     *
     * @param keys key列表
     * @return value集合
     */
    List<String> multiGet(Collection<String> keys);

    /**
     * 根据正则表达式获取所有的key集合
     *
     * @param pattern 正则表达式
     * @return key集合
     */
    Set<String> keys(String pattern);


    /**
     * 删除指定的key
     *
     * @param key 缓存的key
     * @return 是否删除成功
     */
    Boolean delete(String key);

    /**
     * 带参数查询对象和简单类型数据，防止缓存穿透
     *
     * @param keyPrefix  缓存的key前缀
     * @param id         缓存的业务标识
     * @param type       缓存的实际对象类型
     * @param dbFallback 查询数据库的Function函数
     * @param timeout    缓存时长
     * @param unit       缓存时长单位
     * @param <R>        结果泛型
     * @param <ID>       查询数据库参数泛型，也是参数泛型类型
     * @return 业务数据
     */
    <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long timeout, TimeUnit unit);

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
    <R> R queryWithPassThroughWithoutArgs(String keyPrefix, Class<R> type, Supplier<R> dbFallback, Long timeout, TimeUnit unit);

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
    <R, ID> List<R> queryWithPassThroughList(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long timeout, TimeUnit unit);


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
    <R> List<R> queryWithPassThroughListWithoutArgs(String keyPrefix, Class<R> type, Supplier<List<R>> dbFallback, Long timeout, TimeUnit unit);


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
    <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long timeout, TimeUnit unit);


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
    <R> R queryWithLogicalExpireWithoutArgs(String keyPrefix, Class<R> type, Supplier<R> dbFallback, Long timeout, TimeUnit unit);


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
    <R, ID> List<R> queryWithLogicalExpireList(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long timeout, TimeUnit unit);


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
    <R> List<R> queryWithLogicalExpireListWithoutArgs(String keyPrefix, Class<R> type, Supplier<List<R>> dbFallback, Long timeout, TimeUnit unit);

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
    <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long timeout, TimeUnit unit);


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
    <R> R queryWithMutexWithoutArgs(String keyPrefix, Class<R> type, Supplier<R> dbFallback, Long timeout, TimeUnit unit);

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
    <R, ID> List<R> queryWithMutexList(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long timeout, TimeUnit unit);


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
    <R> List<R> queryWithMutexListWithoutArgs(String keyPrefix, Class<R> type, Supplier<List<R>> dbFallback, Long timeout, TimeUnit unit);


    /**
     * 将对象类型的Json字符串转换成泛型类型
     *
     * @param obj  OBJ
     * @param type 类型
     * @return r
     */
    default <R> R getResult(Object obj, Class<R> type) {
        if (obj == null) {
            return null;
        }
        //简单类型
        if (TypeConversion.isSimpleType(obj)) {
            return Convert.convert(type, obj);
        }
        return JSONUtil.toBean(JSONUtil.toJsonStr(obj), type);
    }

    /**
     * 将对象类型的Json字符串转换成泛型类型的List集合
     *
     * @param s    s
     * @param type 类型
     * @return 列表<r>
     */
    default <R> List<R> getResultList(String s, Class<R> type) {
        if (StrUtil.isEmpty(s)) {
            return null;
        }
        return JSONUtil.toList(JSONUtil.parseArray(s), type);
    }

    /**
     * 获取简单的key
     *
     * @param key 缓存的key
     * @return 字符串
     */
    default String getKey(String key) {
        return getKey(key, null);
    }

    /**
     * 不确定参数类型的情况下，使用MD5计算参数的拼接到Redis中的唯一key
     *
     * @param keyPrefix 缓存的key前缀
     * @param id        泛型参数
     * @return 拼接好的缓存key
     */
    default <ID> String getKey(String keyPrefix, ID id) {
        if (id == null) {
            return keyPrefix;
        }
        String key = "";
        //简单数据类型与简单字符串
        if (TypeConversion.isSimpleType(id)) {
            key = StrUtil.toString(id);
        } else {
            key = MD5.create().digestHex(JSONUtil.toJsonStr(id));
        }
        if (StrUtil.isEmpty(key)) {
            key = "";
        }
        return keyPrefix.concat(key);
    }

    /**
     * 获取要保存到缓存中的value字符串，可能是简单类型，也可能是对象类型/集合数组等
     *
     * @param value 缓存的value
     * @return 字符串
     */
    default String getValue(Object value) {
        return TypeConversion.isSimpleType(value) ? String.valueOf(value) : JSONUtil.toJsonStr(value);
    }

}
