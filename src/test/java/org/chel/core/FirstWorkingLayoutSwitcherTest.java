package org.chel.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FirstWorkingLayoutSwitcherTest {

    private final List<String> attempts = new ArrayList<>();

    private LayoutSwitcher strategy(String name, boolean result) {
        return () -> {
            attempts.add(name);
            return result;
        };
    }

    @Test
    void stopsAtTheFirstSuccessfulStrategy() {
        LayoutSwitcher chain = new FirstWorkingLayoutSwitcher(
                strategy("postMessage", true),
                strategy("hotkey", true));

        assertTrue(chain.switchToNextLayout());
        assertEquals(List.of("postMessage"), attempts);
    }

    @Test
    void fallsBackWhenFirstStrategyDoesNotWork() {
        LayoutSwitcher chain = new FirstWorkingLayoutSwitcher(
                strategy("postMessage", false),
                strategy("hotkey", true));

        assertTrue(chain.switchToNextLayout());
        assertEquals(List.of("postMessage", "hotkey"), attempts);
    }

    @Test
    void reportsFailureWhenEveryStrategyFails() {
        LayoutSwitcher chain = new FirstWorkingLayoutSwitcher(
                strategy("postMessage", false),
                strategy("hotkey", false));

        assertFalse(chain.switchToNextLayout());
    }

    @Test
    void continuesAfterStrategyThrows() {
        LayoutSwitcher chain = new FirstWorkingLayoutSwitcher(
                () -> {
                    attempts.add("boom");
                    throw new IllegalStateException("нативный вызов сорвался");
                },
                strategy("hotkey", true));

        assertTrue(assertDoesNotThrow(chain::switchToNextLayout));
        assertEquals(List.of("boom", "hotkey"), attempts);
    }

    @Test
    void rejectsEmptyChain() {
        assertThrows(IllegalArgumentException.class, () -> new FirstWorkingLayoutSwitcher(List.of()));
    }
}
