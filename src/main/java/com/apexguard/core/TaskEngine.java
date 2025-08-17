package com.apexguard.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class TaskEngine {
    private final ExecutorService executor;

    public TaskEngine(final ConfigManager configManager) {
        int configured = configManager.getMaxThreads();
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        int threads = configured > 0 ? configured : Math.max(2, cores - 1);
        int maxQueue = Math.max(8192, configManager.getQueueSize());

        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "ApexGuard-Worker");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        };

        this.executor = new ThreadPoolExecutor(
                threads, threads,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(maxQueue),
                factory,
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    public void submit(Runnable task) {
        executor.submit(task);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}