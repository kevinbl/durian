package com.foo.durian.concurrent;

/**
 * Version 1.0.0 Created by f on 17/2/21.
 */
public abstract class RollbackableRunnable implements Runnable {

    @Override
    public void run() {
        try {
            innerRun();
        } catch (Exception e) {
            try {
                rollback();
            } catch (Exception ignored) {}  // 忽略回滚异常

            throw e;
        }
    }

    protected abstract void innerRun();

    protected abstract void rollback();
}
