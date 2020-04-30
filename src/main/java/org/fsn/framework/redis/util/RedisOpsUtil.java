package org.fsn.framework.redis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author ：Sorta
 * @date ：Created in 2020/4/26
 * @version: 1.0
 **/
@Component
public class RedisOpsUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public void set(String key,Object value){
        redisTemplate.opsForValue().set(key,value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit){
        redisTemplate.opsForValue().set(key,value,timeout,unit);
    }

    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit){
        return redisTemplate.opsForValue().setIfAbsent(key,value,timeout,unit);
    }

    public <T> T get(String key){
        return (T)redisTemplate
                .opsForValue().get(key);
    }

    public String getStr(String key){
        return (String) redisTemplate
                .opsForValue().get(key);
    }

    public Long decr(String key){
        return redisTemplate
                .opsForValue().decrement(key);
    }

    public Long decr(String key,long delta){
        return redisTemplate
                .opsForValue().decrement(key,delta);
    }

    public Long incr(String key){
        return redisTemplate
                .opsForValue().increment(key);
    }

    public Long incr(String key,long delta){
        return redisTemplate
                .opsForValue().increment(key,delta);
    }

}
