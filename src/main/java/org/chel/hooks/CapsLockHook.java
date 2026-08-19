package org.chel.hooks;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.chel.core.CapsLockController;
import org.chel.core.DelayedRunner;
import org.chel.core.LayoutSwitcher;

import java.time.Duration;
import java.util.function.LongSupplier;
import java.util.logging.Logger;

/**
 * По отпусканию Caps Lock переключает раскладку и гасит сам Caps Lock.
 * Shift+Caps пропускается — это штатное включение капса.
 */
public class CapsLockHook implements NativeKeyListener {
    private static final Logger logger = Logger.getLogger(CapsLockHook.class.getName());
    private static final Duration DEBOUNCE = Duration.ofMillis(200);
    private static final Duration CAPS_OFF_DELAY = Duration.ofMillis(100);

    private final LayoutSwitcher layoutSwitcher;
    private final CapsLockController capsLockController;
    private final DelayedRunner delayedRunner;
    private final LongSupplier clock;

    private long lastTrigger = 0;

    public CapsLockHook(LayoutSwitcher layoutSwitcher,
                        CapsLockController capsLockController,
                        DelayedRunner delayedRunner) {
        this(layoutSwitcher, capsLockController, delayedRunner, System::currentTimeMillis);
    }

    CapsLockHook(LayoutSwitcher layoutSwitcher,
                 CapsLockController capsLockController,
                 DelayedRunner delayedRunner,
                 LongSupplier clock) {
        this.layoutSwitcher = layoutSwitcher;
        this.capsLockController = capsLockController;
        this.delayedRunner = delayedRunner;
        this.clock = clock;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
        if (nativeEvent.getKeyCode() != NativeKeyEvent.VC_CAPS_LOCK) {
            return;
        }

        if ((nativeEvent.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) {
            logger.info("Shift+Caps — оставляем штатное поведение");
            return;
        }

        long now = clock.getAsLong();
        if (now - lastTrigger < DEBOUNCE.toMillis()) {
            logger.info("Повторное срабатывание Caps Lock через " + (now - lastTrigger) + " мс, игнорируем");
            return;
        }
        lastTrigger = now;

        try {
            layoutSwitcher.switchToNextLayout();
            delayedRunner.runAfter(CAPS_OFF_DELAY, capsLockController::turnOff);
        } catch (RuntimeException e) {
            logger.severe("Ошибка обработки Caps Lock: " + e.getMessage());
        }
    }
}
