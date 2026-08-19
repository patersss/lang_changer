package org.chel.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest {

    @Test
    void runsActionsInReverseOrderOfRegistration() {
        Lifecycle lifecycle = new Lifecycle();
        List<String> order = new ArrayList<>();

        lifecycle.register("first", () -> order.add("first"));
        lifecycle.register("second", () -> order.add("second"));
        lifecycle.register("third", () -> order.add("third"));

        lifecycle.shutdown();

        assertEquals(List.of("third", "second", "first"), order);
    }

    @Test
    void runsActionsOnlyOnceAcrossRepeatedShutdowns() {
        Lifecycle lifecycle = new Lifecycle();
        AtomicInteger calls = new AtomicInteger();
        lifecycle.register("action", calls::incrementAndGet);

        lifecycle.shutdown();
        lifecycle.shutdown();
        lifecycle.shutdown();

        assertEquals(1, calls.get(), "выход из трея и shutdown hook не должны выполнять очистку дважды");
    }

    @Test
    void continuesAfterFailingAction() {
        Lifecycle lifecycle = new Lifecycle();
        List<String> completed = new ArrayList<>();

        lifecycle.register("ok-before", () -> completed.add("ok-before"));
        lifecycle.register("boom", () -> {
            throw new IllegalStateException("нативный хук уже снят");
        });
        lifecycle.register("ok-after", () -> completed.add("ok-after"));

        assertDoesNotThrow(lifecycle::shutdown);
        assertEquals(List.of("ok-after", "ok-before"), completed);
    }

    @Test
    void ignoresActionsRegisteredAfterShutdown() {
        Lifecycle lifecycle = new Lifecycle();
        AtomicInteger calls = new AtomicInteger();

        lifecycle.shutdown();
        lifecycle.register("late", calls::incrementAndGet);
        lifecycle.shutdown();

        assertEquals(0, calls.get());
    }
}
