package org.fsn.framework.redis.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author ：Sorta
 * @date ：Created in 2020/4/30
 * @version: 1.0
 * @description: 缓存管理工具，采用LRU淘汰策略
 **/
@Component
public class LocalCache {

    private Cache<String, Object> localCache = null;

    @PostConstruct
    private void init(){
        localCache = CacheBuilder.newBuilder()
                //设置本地缓存容器的初始容量
                .initialCapacity(10)
                //设置本地缓存的最大容量
                .maximumSize(500)
                //设置写缓存后多少秒过期
                .expireAfterWrite(60, TimeUnit.SECONDS).build();
    }


    public void setLocalCache(String key,Object object){
        localCache.put(key,object);
    }

    public <T> T get(String key){
       return (T)localCache.getIfPresent(key);
    }

}
