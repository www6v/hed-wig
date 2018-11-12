package com.yihaodian.architecture.hedwig.common.util.concurrent;

import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;

import java.util.concurrent.*;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public HedwigScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public HedwigScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public HedwigScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public HedwigScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return super.schedule(buildHedwigRunnable(task), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return super.scheduleAtFixedRate(buildHedwigRunnable(task), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(buildHedwigRunnable(task), initialDelay, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
        return super.schedule(buildHedwigCallable(task), delay, unit);
    }

    @Override
    public void execute(Runnable task) {
        super.execute(buildHedwigRunnable(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(buildHedwigRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(buildHedwigRunnable(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(buildHedwigCallable(task));
    }

    private HedwigRunnable buildHedwigRunnable(Runnable command) {
        return new HedwigRunnable(HedwigContextUtil.getInvocationContext(), command);
    }

    private <T> HedwigCallable<T> buildHedwigCallable(Callable<T> task) {
        return new HedwigCallable<T>(HedwigContextUtil.getInvocationContext(), task);
    }
}
