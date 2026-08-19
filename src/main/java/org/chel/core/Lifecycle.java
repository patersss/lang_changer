package org.chel.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Единая точка завершения работы приложения. Действия выполняются в порядке,
 * обратном регистрации, ровно один раз — сколько бы раз ни вызвали {@link #shutdown()}
 * (из пункта меню трея и из shutdown hook одновременно).
 */
public final class Lifecycle {
    private static final Logger logger = Logger.getLogger(Lifecycle.class.getName());

    private final Deque<NamedAction> actions = new ArrayDeque<>();
    private final AtomicBoolean alreadyShutDown = new AtomicBoolean(false);

    @FunctionalInterface
    public interface ShutdownAction {
        void run() throws Exception;
    }

    private record NamedAction(String name, ShutdownAction action) {
    }

    public synchronized void register(String name, ShutdownAction action) {
        actions.push(new NamedAction(name, action));
    }

    /**
     * Выполняет все зарегистрированные действия. Сбой одного не мешает остальным.
     */
    public void shutdown() {
        if (!alreadyShutDown.compareAndSet(false, true)) {
            logger.fine("Shutdown already performed, skipping");
            return;
        }

        List<NamedAction> snapshot;
        synchronized (this) {
            snapshot = List.copyOf(actions);
            actions.clear();
        }

        for (NamedAction named : snapshot) {
            try {
                named.action().run();
                logger.info("Shutdown step done: " + named.name());
            } catch (Exception e) {
                logger.warning("Shutdown step failed: " + named.name() + " — " + e.getMessage());
            }
        }
    }

    /**
     * Регистрирует {@link #shutdown()} как JVM shutdown hook, чтобы завершение
     * через System.exit() и через сигнал ОС шло одним и тем же путём.
     */
    public void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "Lifecycle-Shutdown"));
    }
}
