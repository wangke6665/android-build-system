package com.shenma.tvlauncher.tvlive.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程管理
 * 优化：修复线程不安全的单例模式，改进线程池配置
 *
 * @author joychang
 */
public class ThreadPoolManager {
    private static volatile ThreadPoolManager manager;
    private ExecutorService service;

    private ThreadPoolManager() {
        int num = Runtime.getRuntime().availableProcessors();
        // 优化：使用合理的线程池大小，避免过多线程导致上下文切换开销
        int corePoolSize = Math.max(2, num);
        int maximumPoolSize = num * 2 + 1;
        service = Executors.newFixedThreadPool(maximumPoolSize);
    }

    /**
     * 优化：使用双重检查锁定模式确保线程安全
     */
    public static ThreadPoolManager getInstance() {
        if (manager == null) {
            synchronized (ThreadPoolManager.class) {
                if (manager == null) {
                    manager = new ThreadPoolManager();
                }
            }
        }
        return manager;
    }

    public void addTask(Runnable runnable) {
        if (service != null && !service.isShutdown()) {
            service.submit(runnable);
        }
    }

    /**
     * 提交任务并返回Future，用于需要取消任务的场景
     * @param runnable 要执行的任务
     * @return Future对象，可用于取消任务
     */
    public Future<?> submitTask(Runnable runnable) {
        if (service != null && !service.isShutdown()) {
            return service.submit(runnable);
        }
        return null;
    }

    /**
     * 优化：改进方法命名，提供更灵活的关闭选项
     */
    public void shutdown() {
        if (service != null && !service.isShutdown()) {
            service.shutdown();
        }
    }

    /**
     * 优雅关闭：等待正在执行的任务完成
     */
    public void shutdownGracefully() {
        if (service != null && !service.isShutdown()) {
            service.shutdown();
            try {
                if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                    service.shutdownNow();
                }
            } catch (InterruptedException e) {
                service.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 立即关闭：中断所有任务
     */
    public void shutdownNow() {
        if (service != null && !service.isShutdown()) {
            service.shutdownNow();
        }
    }

    /**
     * 保持向后兼容
     * @deprecated 使用 shutdown() 替代
     */
    @Deprecated
    public void removeTask() {
        shutdown();
    }

}
