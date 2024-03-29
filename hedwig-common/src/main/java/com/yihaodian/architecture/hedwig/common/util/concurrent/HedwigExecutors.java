package com.yihaodian.architecture.hedwig.common.util.concurrent;

/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Factory and utility methods for {@link java.util.concurrent.Executor}, {@link
 * java.util.concurrent.ExecutorService}, {@link java.util.concurrent.ScheduledExecutorService}, {@link
 * java.util.concurrent.ThreadFactory}, and {@link java.util.concurrent.Callable} classes defined in this
 * package. This class supports the following kinds of methods:
 *
 * <ul>
 *   <li> Methods that create and return an {@link java.util.concurrent.ExecutorService}
 *        set up with commonly useful configuration settings.
 *   <li> Methods that create and return a {@link java.util.concurrent.ScheduledExecutorService}
 *        set up with commonly useful configuration settings.
 *   <li> Methods that create and return a "wrapped" ExecutorService, that
 *        disables reconfiguration by making implementation-specific methods
 *        inaccessible.
 *   <li> Methods that create and return a {@link java.util.concurrent.ThreadFactory}
 *        that sets newly created threads to a known state.
 *   <li> Methods that create and return a {@link java.util.concurrent.Callable}
 *        out of other closure-like forms, so they can be used
 *        in execution methods requiring <tt>Callable</tt>.
 * </ul>
 *
 * @since 1.5
 * @author Doug Lea,hikin yao
 */
class HedwigExecutors {

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue.  At any point, at most
     * <tt>nThreads</tt> threads will be active processing tasks.
     * If additional tasks are submitted when all threads are active,
     * they will wait in the queue until a thread is available.
     * If any thread terminates due to a failure during execution
     * prior to shutdown, a new one will take its place if needed to
     * execute subsequent tasks.  The threads in the pool will exist
     * until it is explicitly {@link java.util.concurrent.ExecutorService#shutdown shutdown}.
     *
     * @param nThreads the number of threads in the pool
     * @return the newly created thread pool
     * @throws IllegalArgumentException if <tt>nThreads &lt;= 0</tt>
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new HedwigThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.  At any point,
     * at most <tt>nThreads</tt> threads will be active processing
     * tasks.  If additional tasks are submitted when all threads are
     * active, they will wait in the queue until a thread is
     * available.  If any thread terminates due to a failure during
     * execution prior to shutdown, a new one will take its place if
     * needed to execute subsequent tasks.  The threads in the pool will
<<<<<<< HEAD
     * exist until it is explicitly {@link java.util.concurrent.ExecutorService#shutdown
=======
     * exist until it is explicitly {@link ExecutorService#shutdown
>>>>>>> gray
     * shutdown}.
     *
     * @param nThreads the number of threads in the pool
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     * @throws IllegalArgumentException if <tt>nThreads &lt;= 0</tt>
     */
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new HedwigThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
    }

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue. (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * <tt>newFixedThreadPool(1)</tt> the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @return the newly created single-threaded Executor
     */
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
                (new HedwigThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>()));
    }

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed. Unlike the otherwise
     * equivalent <tt>newFixedThreadPool(1, threadFactory)</tt> the
     * returned executor is guaranteed not to be reconfigurable to use
     * additional threads.
     *
     * @param threadFactory the factory to use when creating new
     * threads
     *
     * @return the newly created single-threaded Executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new FinalizableDelegatedExecutorService
                (new HedwigThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(),
                        threadFactory));
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to <tt>execute</tt> will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link HedwigThreadPoolExecutor} constructors.
     *
     * @return the newly created thread pool
     */
    public static ExecutorService newCachedThreadPool() {
        return new HedwigThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available, and uses the provided
     * ThreadFactory to create new threads when needed.
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     */
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new HedwigThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                threadFactory);
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.
     * (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * <tt>newScheduledThreadPool(1)</tt> the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     * @return the newly created scheduled executor
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService
                (new HedwigScheduledThreadPoolExecutor(1));
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.  (Note
     * however that if this single thread terminates due to a failure
     * during execution prior to shutdown, a new one will take its
     * place if needed to execute subsequent tasks.)  Tasks are
     * guaranteed to execute sequentially, and no more than one task
     * will be active at any given time. Unlike the otherwise
     * equivalent <tt>newScheduledThreadPool(1, threadFactory)</tt>
     * the returned executor is guaranteed not to be reconfigurable to
     * use additional threads.
     * @param threadFactory the factory to use when creating new
     * threads
     * @return a newly created scheduled executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatedScheduledExecutorService
                (new HedwigScheduledThreadPoolExecutor(1, threadFactory));
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle.
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if <tt>corePoolSize &lt; 0</tt>
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new HedwigScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle.
     * @param threadFactory the factory to use when the executor
     * creates a new thread.
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if <tt>corePoolSize &lt; 0</tt>
     * @throws NullPointerException if threadFactory is null
     */
    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return new HedwigScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }


    /**
     * Returns an object that delegates all defined {@link
<<<<<<< HEAD
     * java.util.concurrent.ExecutorService} methods to the given executor, but not any
=======
     * ExecutorService} methods to the given executor, but not any
>>>>>>> gray
     * other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * @param executor the underlying implementation
     * @return an <tt>ExecutorService</tt> instance
     * @throws NullPointerException if executor null
     */
    public static ExecutorService unconfigurableExecutorService(ExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedExecutorService(executor);
    }

    /**
     * Returns an object that delegates all defined {@link
<<<<<<< HEAD
     * java.util.concurrent.ScheduledExecutorService} methods to the given executor, but
=======
     * ScheduledExecutorService} methods to the given executor, but
>>>>>>> gray
     * not any other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * @param executor the underlying implementation
     * @return a <tt>ScheduledExecutorService</tt> instance
     * @throws NullPointerException if executor null
     */
    public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedScheduledExecutorService(executor);
    }

    /**
     * Returns a default thread factory used to create new threads.
     * This factory creates all new threads used by an Executor in the
     * same {@link ThreadGroup}. If there is a {@link
<<<<<<< HEAD
     * SecurityManager}, it uses the group of {@link
=======
     * java.lang.SecurityManager}, it uses the group of {@link
>>>>>>> gray
     * System#getSecurityManager}, else the group of the thread
     * invoking this <tt>defaultThreadFactory</tt> method. Each new
     * thread is created as a non-daemon thread with priority set to
     * the smaller of <tt>Thread.NORM_PRIORITY</tt> and the maximum
     * priority permitted in the thread group.  New threads have names
     * accessible via {@link Thread#getName} of
     * <em>pool-N-thread-M</em>, where <em>N</em> is the sequence
     * number of this factory, and <em>M</em> is the sequence number
     * of the thread created by this factory.
     * @return a thread factory
     */
    public static ThreadFactory defaultThreadFactory() {
        return new DefaultThreadFactory();
    }

    /**
     * Returns a thread factory used to create new threads that
     * have the same permissions as the current thread.
     * This factory creates threads with the same settings as {@link
<<<<<<< HEAD
     * java.util.concurrent.Executors#defaultThreadFactory}, additionally setting the
=======
     * Executors#defaultThreadFactory}, additionally setting the
>>>>>>> gray
     * AccessControlContext and contextClassLoader of new threads to
     * be the same as the thread invoking this
     * <tt>privilegedThreadFactory</tt> method.  A new
     * <tt>privilegedThreadFactory</tt> can be created within an
     * {@link java.security.AccessController#doPrivileged} action setting the
     * current thread's access control context to create threads with
     * the selected permission settings holding within that action.
     *
     * <p> Note that while tasks running within such threads will have
     * the same access control and class loader settings as the
     * current thread, they need not have the same {@link
<<<<<<< HEAD
     * ThreadLocal} or {@link
     * InheritableThreadLocal} values. If necessary,
=======
     * java.lang.ThreadLocal} or {@link
     * java.lang.InheritableThreadLocal} values. If necessary,
>>>>>>> gray
     * particular values of thread locals can be set or reset before
     * any task runs in {@link HedwigThreadPoolExecutor} subclasses using
     * {@link HedwigThreadPoolExecutor#beforeExecute}. Also, if it is
     * necessary to initialize worker threads to have the same
     * InheritableThreadLocal settings as some other designated
     * thread, you can create a custom ThreadFactory in which that
     * thread waits for and services requests to create others that
     * will inherit its values.
     *
     * @return a thread factory
     * @throws java.security.AccessControlException if the current access control
     * context does not have permission to both get and set context
     * class loader.
     */
    public static ThreadFactory privilegedThreadFactory() {
        return new PrivilegedThreadFactory();
    }

    /**
<<<<<<< HEAD
     * Returns a {@link java.util.concurrent.Callable} object that, when
=======
     * Returns a {@link Callable} object that, when
>>>>>>> gray
     * called, runs the given task and returns the given result.  This
     * can be useful when applying methods requiring a
     * <tt>Callable</tt> to an otherwise resultless action.
     * @param task the task to run
     * @param result the result to return
     * @return a callable object
     * @throws NullPointerException if task null
     */
    public static <T> Callable<T> callable(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<T>(task, result);
    }

    /**
<<<<<<< HEAD
     * Returns a {@link java.util.concurrent.Callable} object that, when
=======
     * Returns a {@link Callable} object that, when
>>>>>>> gray
     * called, runs the given task and returns <tt>null</tt>.
     * @param task the task to run
     * @return a callable object
     * @throws NullPointerException if task null
     */
    public static Callable<Object> callable(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<Object>(task, null);
    }

    /**
<<<<<<< HEAD
     * Returns a {@link java.util.concurrent.Callable} object that, when
=======
     * Returns a {@link Callable} object that, when
>>>>>>> gray
     * called, runs the given privileged action and returns its result.
     * @param action the privileged action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    public static Callable<Object> callable(final PrivilegedAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() { return action.run(); }};
    }

    /**
<<<<<<< HEAD
     * Returns a {@link java.util.concurrent.Callable} object that, when
=======
     * Returns a {@link Callable} object that, when
>>>>>>> gray
     * called, runs the given privileged exception action and returns
     * its result.
     * @param action the privileged exception action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    public static Callable<Object> callable(final PrivilegedExceptionAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() throws Exception { return action.run(); }};
    }

    /**
<<<<<<< HEAD
     * Returns a {@link java.util.concurrent.Callable} object that will, when
=======
     * Returns a {@link Callable} object that will, when
>>>>>>> gray
     * called, execute the given <tt>callable</tt> under the current
     * access control context. This method should normally be
     * invoked within an {@link java.security.AccessController#doPrivileged} action
     * to create callables that will, if possible, execute under the
     * selected permission settings holding within that action; or if
     * not possible, throw an associated {@link
     * java.security.AccessControlException}.
     * @param callable the underlying task
     * @return a callable object
     * @throws NullPointerException if callable null
     *
     */
    public static <T> Callable<T> privilegedCallable(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallable<T>(callable);
    }

    /**
<<<<<<< HEAD
     * Returns a {@link java.util.concurrent.Callable} object that will, when
=======
     * Returns a {@link Callable} object that will, when
>>>>>>> gray
     * called, execute the given <tt>callable</tt> under the current
     * access control context, with the current context class loader
     * as the context class loader. This method should normally be
     * invoked within an {@link java.security.AccessController#doPrivileged} action
     * to create callables that will, if possible, execute under the
     * selected permission settings holding within that action; or if
     * not possible, throw an associated {@link
     * java.security.AccessControlException}.
     * @param callable the underlying task
     *
     * @return a callable object
     * @throws NullPointerException if callable null
     * @throws java.security.AccessControlException if the current access control
     * context does not have permission to both set and get context
     * class loader.
     */
    public static <T> Callable<T> privilegedCallableUsingCurrentClassLoader(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallableUsingCurrentClassLoader<T>(callable);
    }

    // Non-public classes supporting the public methods

    /**
     * A callable that runs given task and returns given result
     */
    static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;
        RunnableAdapter(Runnable  task, T result) {
            this.task = task;
            this.result = result;
        }
        public T call() {
            task.run();
            return result;
        }
    }

    /**
     * A callable that runs under established access control settings
     */
    static final class PrivilegedCallable<T> implements Callable<T> {
        private final AccessControlContext acc;
        private final Callable<T> task;
        private T result;
        private Exception exception;
        PrivilegedCallable(Callable<T> task) {
            this.task = task;
            this.acc = AccessController.getContext();
        }

        public T call() throws Exception {
            AccessController.doPrivileged(new PrivilegedAction<T>() {
                public T run() {
                    try {
                        result = task.call();
                    } catch (Exception ex) {
                        exception = ex;
                    }
                    return null;
                }
            }, acc);
            if (exception != null)
                throw exception;
            else
                return result;
        }
    }

    /**
     * A callable that runs under established access control settings and
     * current ClassLoader
     */
    static final class PrivilegedCallableUsingCurrentClassLoader<T> implements Callable<T> {
        private final ClassLoader ccl;
        private final AccessControlContext acc;
        private final Callable<T> task;
        private T result;
        private Exception exception;
        PrivilegedCallableUsingCurrentClassLoader(Callable<T> task) {
            this.task = task;
            this.ccl = Thread.currentThread().getContextClassLoader();
            this.acc = AccessController.getContext();
            acc.checkPermission(new RuntimePermission("getContextClassLoader"));
            acc.checkPermission(new RuntimePermission("setContextClassLoader"));
        }

        public T call() throws Exception {
            AccessController.doPrivileged(new PrivilegedAction<T>() {
                public T run() {
                    Thread t = Thread.currentThread();
                    try {
                        ClassLoader cl = t.getContextClassLoader();
                        if (ccl == cl) {
                            result = task.call();
                        } else {
                            t.setContextClassLoader(ccl);
                            try {
                                result = task.call();
                            } finally {
                                t.setContextClassLoader(cl);
                            }
                        }
                    } catch (Exception ex) {
                        exception = ex;
                    }
                    return null;
                }
            }, acc);
            if (exception != null)
                throw exception;
            else
                return result;
        }
    }

    /**
     * The default thread factory
     */
    static class DefaultThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     *  Thread factory capturing access control and class loader
     */
    static class PrivilegedThreadFactory extends DefaultThreadFactory {
        private final ClassLoader ccl;
        private final AccessControlContext acc;

        PrivilegedThreadFactory() {
            super();
            this.ccl = Thread.currentThread().getContextClassLoader();
            this.acc = AccessController.getContext();
            acc.checkPermission(new RuntimePermission("setContextClassLoader"));
        }

        public Thread newThread(final Runnable r) {
            return super.newThread(new Runnable() {
                public void run() {
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            Thread.currentThread().setContextClassLoader(ccl);
                            r.run();
                            return null;
                        }
                    }, acc);
                }
            });
        }

    }

    /**
     * A wrapper class that exposes only the ExecutorService methods
     * of an ExecutorService implementation.
     */
    static class DelegatedExecutorService extends AbstractExecutorService {
        private final ExecutorService e;
        DelegatedExecutorService(ExecutorService executor) { e = executor; }
        public void execute(Runnable command) { e.execute(command); }
        public void shutdown() { e.shutdown(); }
        public List<Runnable> shutdownNow() { return e.shutdownNow(); }
        public boolean isShutdown() { return e.isShutdown(); }
        public boolean isTerminated() { return e.isTerminated(); }
        public boolean awaitTermination(long timeout, TimeUnit unit)
                throws InterruptedException {
            return e.awaitTermination(timeout, unit);
        }
        public Future<?> submit(Runnable task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Callable<T> task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Runnable task, T result) {
            return e.submit(task, result);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            return e.invokeAll(tasks);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                             long timeout, TimeUnit unit)
                throws InterruptedException {
            return e.invokeAll(tasks, timeout, unit);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
            return e.invokeAny(tasks);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                               long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return e.invokeAny(tasks, timeout, unit);
        }
    }

    static class FinalizableDelegatedExecutorService
            extends DelegatedExecutorService {
        FinalizableDelegatedExecutorService(ExecutorService executor) {
            super(executor);
        }
        protected void finalize()  {
            super.shutdown();
        }
    }

    /**
     * A wrapper class that exposes only the ScheduledExecutorService
     * methods of a ScheduledExecutorService implementation.
     */
    static class DelegatedScheduledExecutorService
            extends DelegatedExecutorService
            implements ScheduledExecutorService {
        private final ScheduledExecutorService e;
        DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
            super(executor);
            e = executor;
        }
        public ScheduledFuture<?> schedule(Runnable command, long delay,  TimeUnit unit) {
            return e.schedule(command, delay, unit);
        }
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return e.schedule(callable, delay, unit);
        }
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,  long period, TimeUnit unit) {
            return e.scheduleAtFixedRate(command, initialDelay, period, unit);
        }
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,  long delay, TimeUnit unit) {
            return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }


    /** Cannot instantiate. */
    private HedwigExecutors() {}
}
