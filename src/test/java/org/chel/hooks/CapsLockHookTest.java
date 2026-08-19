package org.chel.hooks;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.chel.core.CapsLockController;
import org.chel.core.DelayedRunner;
import org.chel.core.LayoutSwitcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapsLockHookTest {

    private final AtomicInteger switchCalls = new AtomicInteger();
    private final AtomicInteger turnOffCalls = new AtomicInteger();
    private long now;
    private CapsLockHook hook;

    private final LayoutSwitcher layoutSwitcher = () -> {
        switchCalls.incrementAndGet();
        return true;
    };

    private final CapsLockController capsLockController = new CapsLockController() {
        @Override
        public boolean isOn() {
            return true;
        }

        @Override
        public void turnOff() {
            turnOffCalls.incrementAndGet();
        }
    };

    /** выполняет отложенную задачу сразу, чтобы тест не зависел от таймингов */
    private final DelayedRunner immediateRunner = (_, task) -> task.run();

    @BeforeEach
    void setUp() {
        now = 1_000L;
        hook = new CapsLockHook(layoutSwitcher, capsLockController, immediateRunner, () -> now);
    }

    private static NativeKeyEvent release(int keyCode, int modifiers) {
        return new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_RELEASED, modifiers, 0, keyCode, ' ');
    }

    @Test
    void switchesLayoutAndClearsCapsLockOnCapsRelease() {
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));

        assertEquals(1, switchCalls.get());
        assertEquals(1, turnOffCalls.get());
    }

    @Test
    void ignoresOtherKeys() {
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_A, 0));

        assertEquals(0, switchCalls.get());
        assertEquals(0, turnOffCalls.get());
    }

    @Test
    void keepsDefaultBehaviourForShiftCaps() {
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, NativeKeyEvent.SHIFT_MASK));

        assertEquals(0, switchCalls.get(), "Shift+Caps должен штатно включать капс, а не менять раскладку");
        assertEquals(0, turnOffCalls.get());
    }

    @Test
    void debouncesRepeatedTriggersWithinWindow() {
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));
        now += 150;
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));

        assertEquals(1, switchCalls.get(), "дребезг не должен давать двойную смену раскладки");
    }

    @Test
    void allowsTriggerAfterDebounceWindow() {
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));
        now += 250;
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));

        assertEquals(2, switchCalls.get());
    }

    @Test
    void debounceIsNotResetByIgnoredShiftCaps() {
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));
        now += 100;
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, NativeKeyEvent.SHIFT_MASK));
        now += 150; // 250 мс от первого срабатывания
        hook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));

        assertEquals(2, switchCalls.get());
    }

    @Test
    void survivesFailureInLayoutSwitcher() {
        CapsLockHook failingHook = new CapsLockHook(
                () -> {
                    throw new IllegalStateException("нативный вызов сорвался");
                },
                capsLockController, immediateRunner, () -> now);

        failingHook.nativeKeyReleased(release(NativeKeyEvent.VC_CAPS_LOCK, 0));

        assertEquals(0, turnOffCalls.get());
    }
}
