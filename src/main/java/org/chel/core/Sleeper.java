package org.chel.core;

import java.time.Duration;

/**
 * Пауза, вынесенная в интерфейс, чтобы опрос состояния можно было
 * прогонять в тестах без реального ожидания.
 */
@FunctionalInterface
public interface Sleeper {
    Sleeper REAL = duration -> Thread.sleep(duration.toMillis());

    void sleep(Duration duration) throws InterruptedException;
}
