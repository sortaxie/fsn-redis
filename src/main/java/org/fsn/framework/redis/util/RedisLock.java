package org.fsn.framework.redis.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.Charset;

@Component
public class RedisLock {
    Logger log = LoggerFactory.getLogger(RedisLock.class);
    @Resource
    private RedisTemplate redisTemplate;

    @Value("${fsn.redis.waitLock.sleepTnterval:100}")
    private Long sleepTnterval;

    public static final String UNLOCK_LUA;

   // private static final long DEFAULTWAITTIME = 30000L; //默认30s 等待时间


    /**
     * 释放锁脚本，原子操作
     */
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    /**
     * 获取分布式锁，原子操作
     *
     * @param lockKey
     * @param requestId 唯一ID, 可以使用UUID.randomUUID().toString();
     * @param expire    过期时间毫秒
     * @return
     */
    public boolean getLock(String lockKey, String requestId, long expire) {
        try {
            RedisCallback<Boolean> callback = (connection) -> {
                return connection.set(lockKey.getBytes(Charset.forName("UTF-8")), requestId.getBytes(Charset.forName("UTF-8")), Expiration.milliseconds(expire), RedisStringCommands.SetOption.SET_IF_ABSENT);
            };
            return (Boolean) redisTemplate.execute(callback);
        } catch (Exception e) {
            log.error("redis lock error.", e);
        }
        return false;
    }

    /**
     *  等待分布式锁，原子操作
     * @param lockKey
     * @param requestId 唯一ID, 可以使用UUID.randomUUID().toString();
     * @param expire 过期时间毫秒，也是等待时间
     * @return
     */
    public boolean getWaitLock(String lockKey, String requestId, long waitTime,long expire) {
        boolean result = false;
        long waitedTime = 0L; //已经等待时间
        while (true) {
            try {
                long startTime = System.currentTimeMillis();
                RedisCallback<Boolean> callback = (connection) -> {
                    return connection.set(lockKey.getBytes(Charset.forName("UTF-8")), requestId.getBytes(Charset.forName("UTF-8")), Expiration.milliseconds(expire), RedisStringCommands.SetOption.SET_IF_ABSENT);
                };
                result = (Boolean) redisTemplate.execute(callback);
                long endTime = System.currentTimeMillis();
                if (!result) {
                    Thread.sleep(sleepTnterval);
                    waitedTime = waitedTime + (endTime-startTime)+ sleepTnterval;
                    if (waitedTime >= waitTime) {
                        result = false;
                        break;
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                log.error("redis lock error.", e);
                break;
            }
        }
        return result;
    }

    public boolean getWaitLock(String lockKey, String requestId,long expire) {
            return getWaitLock(lockKey,requestId,expire,expire);
    }

    /**
     * 释放锁
     *
     * @param lockKey
     * @param requestId 唯一ID
     * @return
     */
    public boolean releaseLock(String lockKey, String requestId) {
        RedisCallback<Boolean> callback = (connection) -> {
            return connection.eval(UNLOCK_LUA.getBytes(), ReturnType.BOOLEAN, 1, lockKey.getBytes(Charset.forName("UTF-8")), requestId.getBytes(Charset.forName("UTF-8")));
        };
        return (Boolean) redisTemplate.execute(callback);
    }

    /**
     * 获取Redis锁的value值
     *
     * @param lockKey
     * @return
     */
    public String get(String lockKey) {
        try {
            RedisCallback<String> callback = (connection) -> {
                return new String(connection.get(lockKey.getBytes()), Charset.forName("UTF-8"));
            };
            return (String) redisTemplate.execute(callback);
        } catch (Exception e) {
            log.error("get redis occurred an exception", e);
        }
        return null;
    }
}
