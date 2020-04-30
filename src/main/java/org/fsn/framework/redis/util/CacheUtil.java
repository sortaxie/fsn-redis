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

    public void set(String key, Object object) {
        try {
            localCache.setLocalCache(key, object);
            redisOpsUtil.set(key, object, 3600, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("cacheError:", e);
        }
    }

    public <T> T get(String key) {
        Object object = localCache.get(key);
        if (object == null) {
            object = redisOpsUtil.get(key);
        }
        return (T) object;
    }
}
