package org.fsn.framework.redis.util;

import org.fsn.framework.redis.cache.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheUtil {
    Logger logger = LoggerFactory.getLogger(CacheUtil.class);
    @Autowired
    private LocalCache localCache;
    @Autowired
    private RedisOpsUtil redisOpsUtil;


    public void set(String key, Object object,long timeout) {
        try {
            localCache.setLocalCache(key, object);
            redisOpsUtil.set(key, object, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("cacheError:", e);
        }
    }

    public void set(String key, Object object) {
        set(key,object,3600);
    }

    public <T> T get(String key) {
        Object object = localCache.get(key);
        if (object == null) {
            object = redisOpsUtil.get(key);
            if(object!=null) {
                localCache.setLocalCache(key, object);
            }else{
                return null;
            }
        }
        return (T) object;
    }

    public void delete(String key){
        localCache.delete(key);
        redisOpsUtil.delete(key);
    }
}
