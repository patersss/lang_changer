package org.chel.core;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Боевая реализация {@link DelayedRunner} на одном демон-потоке.
 */
public final class SchedulerDelayedRunner implements DelayedRunner, AutoCloseable {
    private final ScheduledExecutorService scheduler;

    public SchedulerDelayedRunner(String threadName) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void runAfter(Duration delay, Runnable task) {
        scheduler.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
