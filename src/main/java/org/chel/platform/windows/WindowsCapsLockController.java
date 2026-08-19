package org.chel.platform.windows;

import org.chel.core.CapsLockController;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Гасит Caps Lock эмуляцией его нажатия. Windows применяет toggle не мгновенно,
 * поэтому после отправки состояние перепроверяется, и при неудаче делается ещё попытка.
 */
public final class WindowsCapsLockController implements CapsLockController {
    private static final Logger logger = Logger.getLogger(WindowsCapsLockController.class.getName());
    private static final Duration DEFAULT_VERIFY_DELAY = Duration.ofMillis(100);
    private static final int MAX_ATTEMPTS = 2;

    private final Win32Keyboard keyboard;
    private final Duration verifyDelay;

    public WindowsCapsLockController(Win32Keyboard keyboard) {
        this(keyboard, DEFAULT_VERIFY_DELAY);
    }

    public WindowsCapsLockController(Win32Keyboard keyboard, Duration verifyDelay) {
        this.keyboard = keyboard;
        this.verifyDelay = verifyDelay;
    }

    @Override
    public boolean isOn() {
        return keyboard.isToggleOn(WindowsKeyCodes.VK_CAPITAL);
    }

    @Override
    public void turnOff() {
        if (!isOn()) {
            logger.fine("Caps Lock уже выключен");
            return;
        }

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            keyboard.sendKeyCombo(List.of(WindowsKeyCodes.VK_CAPITAL));
            if (!awaitAndCheck()) {
                logger.info("Caps Lock выключен с попытки " + attempt);
                return;
            }
            logger.warning("Caps Lock всё ещё включён после попытки " + attempt);
        }
        logger.severe("Не удалось выключить Caps Lock за " + MAX_ATTEMPTS + " попытки");
    }

    /**
     * @return состояние Caps Lock после паузы
     */
    private boolean awaitAndCheck() {
        if (!verifyDelay.isZero() && !verifyDelay.isNegative()) {
            try {
                Thread.sleep(verifyDelay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return isOn();
            }
        }
        return isOn();
    }
}
