package org.fsn.framework.redis.util;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
    //需要外层try finally  可以重入锁
    RLock lock(String lockKey);

    RLock lock(String lockKey, long leaseTime);

    RLock lock(String lockKey, TimeUnit unit, long leaseTime);

    boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime);

    boolean tryLock(String lockKey,  long waitTime, long leaseTime);
    boolean tryLock(String lockKey,  long leaseTime);
    boolean tryLock(String lockKey);
    //需要外层try finally
    boolean tryWaitLock(String lockKey,long waitTime);
    void unlock(String lockKey);

    void unlock(RLock lock);

}
