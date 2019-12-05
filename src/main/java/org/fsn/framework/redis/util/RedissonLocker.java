package org.fsn.framework.redis.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "framework.redis.redisson", value = {"enable"}, havingValue = "true")
public class RedissonLocker  implements  DistributedLock{
    @Resource
    private RedissonClient redissonClient;  //RedissonClient已经由配置类生成，这里自动装配即可

    //lock(), 拿不到就一直block
    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    //leaseTime为加锁时间，单位为毫秒
    @Override
    public RLock lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, TimeUnit.MILLISECONDS);
        return lock;
    }

    //timeout为加锁时间，时间单位由unit确定
    @Override
    public RLock lock(String lockKey, TimeUnit unit ,long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, unit);
        return lock;
    }
    //tryLock()，马上返回，拿到lock就返回true，不然返回false。
    //带时间限制的tryLock()，拿不到lock，就等一段时间，超时返回false.
    //waitTime 等待时间  leaseTime 释放锁时间
    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        return tryLock(lockKey,TimeUnit.MILLISECONDS,waitTime,leaseTime);
    }

    @Override
    public boolean tryLock(String lockKey, long leaseTime) {
        return tryLock(lockKey,0,leaseTime);
    }

    @Override
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey,0,-1);
    }

    @Override
    public boolean tryWaitLock(String lockKey, long waitTime) {
        return tryLock(lockKey,waitTime,-1);
    }


    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }


}
