package io.kevinz613.pear.cache.model;


import io.kevinz613.pear.cache.model.base.CommonCache;

/**
 * 业务数据缓存
 *
 * @author kevinz613
 */
public class BusinessCache<T> extends CommonCache {

    //业务数据
    private T data;

    public BusinessCache<T> with(T data) {
        this.data = data;
        this.exist = true;
        return this;
    }

    public BusinessCache<T> withVersion(Long version) {
        this.version = version;
        return this;
    }

    public BusinessCache<T> retryLater() {
        this.retryLater = true;
        return this;
    }

    public BusinessCache<T> notExist() {
        this.exist = false;
        return this;
    }
}
