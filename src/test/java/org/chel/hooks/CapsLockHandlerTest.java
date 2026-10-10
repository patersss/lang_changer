package org.chel.hooks;

import org.chel.core.Key;
import org.chel.core.KeyStroke;
import org.chel.core.LayoutSwitcher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CapsLockHandlerTest {

    /** выполняет задачу сразу, чтобы тест не зависел от планировщика */
    private static final Executor IMMEDIATE = Runnable::run;

    private final AtomicInteger switchCalls = new AtomicInteger();
    private final LayoutSwitcher switcher = () -> {
        switchCalls.incrementAndGet();
        return true;
    };

    private CapsLockHandler handler() {
        return new CapsLockHandler(switcher, IMMEDIATE);
    }

    private static KeyStroke caps(boolean pressed, boolean shiftDown, boolean injected) {
        return new KeyStroke(Key.CAPS_LOCK, pressed, shiftDown, injected);
    }

    @Test
    void suppressesCapsAndSwitchesLayout() {
        assertTrue(handler().onKeyStroke(caps(true, false, false)));
        assertEquals(1, switchCalls.get());
    }

    @Test
    void keepsDefaultBehaviourForShiftCaps() {
        assertFalse(handler().onKeyStroke(caps(true, true, false)),
                "Shift+Caps должен штатно включать капс");
        assertEquals(0, switchCalls.get());
    }

    @Test
    void ignoresOwnSyntheticInput() {
        assertFalse(handler().onKeyStroke(caps(true, false, true)),
                "свой SendInput из запасной стратегии не должен запускать новый цикл");
        assertEquals(0, switchCalls.get());
    }

    @Test
    void suppressesReleaseWithoutSwitchingAgain() {
        CapsLockHandler handler = handler();

        handler.onKeyStroke(caps(true, false, false));
        assertTrue(handler.onKeyStroke(caps(false, false, false)));

        assertEquals(1, switchCalls.get());
    }

    @Test
    void stillSuppressesWhenSwitchingFails() {
        CapsLockHandler failing = new CapsLockHandler(() -> false, IMMEDIATE);

        assertTrue(failing.onKeyStroke(caps(true, false, false)),
                "иначе неудачное переключение включило бы капс");
    }

    @Test
    void survivesExceptionFromSwitcher() {
        CapsLockHandler failing = new CapsLockHandler(() -> {
            throw new IllegalStateException("нативный вызов сорвался");
        }, IMMEDIATE);

        assertTrue(assertDoesNotThrow(() -> failing.onKeyStroke(caps(true, false, false))));
    }
}
