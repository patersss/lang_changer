package org.chel.hooks;

import org.chel.core.HotkeyListener;
import org.chel.core.Key;
import org.chel.core.KeyStroke;
import org.chel.core.LayoutSwitcher;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Решает судьбу нажатия Caps Lock: поглотить его и переключить раскладку
 * либо пропустить в систему.
 * <p>
 * Сама смена раскладки уходит в отдельный поток: обработчик вызывается из
 * низкоуровневого хука, а тот обязан вернуться в пределах LowLevelHooksTimeout,
 * иначе Windows снимет хук без единого сообщения об ошибке.
 */
public final class CapsLockHandler implements HotkeyListener {
    private static final Logger logger = Logger.getLogger(CapsLockHandler.class.getName());

    private final LayoutSwitcher layoutSwitcher;
    private final Executor executor;

    public CapsLockHandler(LayoutSwitcher layoutSwitcher, Executor executor) {
        this.layoutSwitcher = layoutSwitcher;
        this.executor = executor;
    }

    @Override
    public boolean onKeyStroke(KeyStroke stroke) {
        if (stroke.key() != Key.CAPS_LOCK) {
            return false;
        }
        // собственный SendInput из запасной стратегии не должен запускать новый цикл:
        // именно это давало двойную смену языка ru -> en -> ru
        if (stroke.injected()) {
            return false;
        }
        // Shift+Caps остаётся штатным включением капса
        if (stroke.shiftDown()) {
            return false;
        }
        if (!stroke.pressed()) {
            return true;
        }

        executor.execute(() -> {
            try {
                if (!layoutSwitcher.switchToNextLayout()) {
                    logger.warning("Раскладку переключить не удалось");
                }
            } catch (RuntimeException e) {
                logger.severe("Ошибка при переключении раскладки: " + e.getMessage());
            }
        });
        return true;
    }
}
