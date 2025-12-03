package com.leung.rabbitmqhard.middleware.rabbitmq.util;

import jakarta.annotation.Resource;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redisson的Redis工具类
 */
@Component
public class RedissonUtil {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 设置字符串值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    /**
     * 设置字符串值并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeout, unit);
    }

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 删除键值对
     *
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redissonClient.getBucket(key).expire(timeout, unit);
    }

    /**
     * 获取过期剩余时间
     *
     * @param key 键
     * @return 剩余时间
     */
    public long getExpire(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }

    /**
     * 原子递增
     *
     * @param key 键
     * @return 递增后的值
     */
    public long increment(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    /**
     * 原子递减
     *
     * @param key 键
     * @return 递减后的值
     */
    public long decrement(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    /**
     * 获取分布式锁
     *
     * @param key 锁名称
     * @return RLock对象
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * 获取公平锁
     *
     * @param key 锁名称
     * @return RLock对象
     */
    public RLock getFairLock(String key) {
        return redissonClient.getFairLock(key);
    }

    /**
     * 获取读锁
     *
     * @param key 锁名称
     * @return RReadWriteLock对象
     */
    public RReadWriteLock getReadWriteLock(String key) {
        return redissonClient.getReadWriteLock(key);
    }

    /**
     * 向集合中添加元素
     *
     * @param key   集合key
     * @param value 元素值
     * @return 是否添加成功
     */
    public boolean setAdd(String key, Object value) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.add(value);
    }

    /**
     * 获取集合大小
     *
     * @param key 集合key
     * @return 集合大小
     */
    public int setSize(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }

    /**
     * 判断集合是否包含某个元素
     *
     * @param key   集合key
     * @param value 元素值
     * @return 是否包含
     */
    public boolean setIsMember(String key, Object value) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.contains(value);
    }
}
