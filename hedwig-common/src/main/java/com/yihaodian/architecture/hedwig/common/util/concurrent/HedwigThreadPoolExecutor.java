package com.yihaodian.architecture.hedwig.common.util.concurrent;

import com.yihaodian.architecture.hedwig.common.util.HedwigContextUtil;

import java.util.concurrent.*;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class HedwigThreadPoolExecutor extends ThreadPoolExecutor {
    public HedwigThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public HedwigThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public HedwigThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public HedwigThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable task) {
        super.execute(buildHedwigRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(buildHedwigCallable(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(buildHedwigRunnable(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(buildHedwigRunnable(task));
    }

    private HedwigRunnable buildHedwigRunnable(Runnable command) {
        return new HedwigRunnable(HedwigContextUtil.getInvocationContext(), command);
    }

    private <T> HedwigCallable<T> buildHedwigCallable(Callable<T> task) {
        return new HedwigCallable<T>(HedwigContextUtil.getInvocationContext(), task);
    }
}
