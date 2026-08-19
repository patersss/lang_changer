package org.chel.core;

import java.time.Duration;

/**
 * Отложенный запуск задачи. Вынесен в интерфейс, чтобы логику, зависящую от таймингов,
 * можно было выполнять синхронно в тестах.
 */
@FunctionalInterface
public interface DelayedRunner {
    void runAfter(Duration delay, Runnable task);
}
