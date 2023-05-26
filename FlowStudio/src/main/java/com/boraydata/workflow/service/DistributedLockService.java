package com.boraydata.workflow.service;

public interface DistributedLockService {
    /**
     * 尝试获取锁
     * @param tag 锁的键
     * @return
     */
    boolean tryLock(String tag);

    /**
     * 释放锁
     * @param tag 锁的键
     */
    void unlock(String tag);
}
