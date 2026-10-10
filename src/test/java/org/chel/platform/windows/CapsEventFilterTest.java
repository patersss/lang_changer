package org.chel.platform.windows;

import org.chel.core.KeyStroke;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CapsEventFilterTest {

    private final List<KeyStroke> seen = new ArrayList<>();

    private CapsEventFilter filterThatConsumes() {
        return new CapsEventFilter(stroke -> {
            seen.add(stroke);
            return true;
        });
    }

    private CapsEventFilter filterThatPassesThrough() {
        return new CapsEventFilter(stroke -> {
            seen.add(stroke);
            return false;
        });
    }

    @Test
    void suppressesPressAndMatchingRelease() {
        CapsEventFilter filter = filterThatConsumes();

        assertTrue(filter.shouldSuppress(true, false, false));
        assertTrue(filter.shouldSuppress(false, false, false),
                "отпускание без нажатия оставило бы приложениям осиротевший key-up");
    }

    @Test
    void releaseIsNotSuppressedWhenPressWasNot() {
        CapsEventFilter filter = filterThatPassesThrough();

        assertFalse(filter.shouldSuppress(true, false, false));
        assertFalse(filter.shouldSuppress(false, false, false));
    }

    @Test
    void notifiesListenerOncePerHoldDespiteAutoRepeat() {
        CapsEventFilter filter = filterThatConsumes();

        filter.shouldSuppress(true, false, false);
        filter.shouldSuppress(true, false, false); // автоповтор
        filter.shouldSuppress(true, false, false); // автоповтор

        assertEquals(1, seen.size(), "удержание Caps Lock не должно менять язык пачкой");
    }

    @Test
    void autoRepeatKeepsSuppressingWhilePressWasSuppressed() {
        CapsEventFilter filter = filterThatConsumes();

        filter.shouldSuppress(true, false, false);
        assertTrue(filter.shouldSuppress(true, false, false));
    }

    @Test
    void notifiesAgainAfterReleaseAndNewPress() {
        CapsEventFilter filter = filterThatConsumes();

        filter.shouldSuppress(true, false, false);
        filter.shouldSuppress(false, false, false);
        filter.shouldSuppress(true, false, false);

        assertEquals(2, seen.size());
    }

    @Test
    void passesShiftAndInjectedFlagsToListener() {
        CapsEventFilter filter = filterThatConsumes();

        filter.shouldSuppress(true, true, true);

        assertTrue(seen.getFirst().shiftDown());
        assertTrue(seen.getFirst().injected());
    }

    @Test
    void doesNotSuppressWhenListenerThrows() {
        CapsEventFilter filter = new CapsEventFilter(_ -> {
            throw new IllegalStateException("обработчик сломался");
        });

        assertFalse(assertDoesNotThrow(() -> filter.shouldSuppress(true, false, false)),
                "при сбое обработчика клавиша должна дойти до системы, а не пропасть");
        assertFalse(filter.shouldSuppress(false, false, false));
    }
}
