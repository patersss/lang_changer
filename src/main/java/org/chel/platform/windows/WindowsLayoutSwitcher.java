package org.chel.platform.windows;

import org.chel.core.LayoutSwitcher;

import java.util.List;
import java.util.logging.Logger;

/**
 * Переключает раскладку эмуляцией системной горячей клавиши, перебирая комбинации,
 * пока одна не сработает.
 * <p>
 * ВНИМАНИЕ: успех определяется по числу событий, принятых SendInput, а это лишь
 * означает «система приняла ввод», а не «раскладка сменилась». Практически всегда
 * срабатывает первая комбинация, поэтому перебор пока декоративный. Настоящая проверка
 * (сравнение GetKeyboardLayout до и после) — отдельный шаг.
 */
public final class WindowsLayoutSwitcher implements LayoutSwitcher {
    private static final Logger logger = Logger.getLogger(WindowsLayoutSwitcher.class.getName());

    private final Win32Keyboard keyboard;
    private final List<HotKeyCombo> combos;

    public WindowsLayoutSwitcher(Win32Keyboard keyboard) {
        this(keyboard, HotKeyCombo.defaults());
    }

    public WindowsLayoutSwitcher(Win32Keyboard keyboard, List<HotKeyCombo> combos) {
        this.keyboard = keyboard;
        this.combos = List.copyOf(combos);
    }

    @Override
    public boolean switchToNextLayout() {
        for (HotKeyCombo combo : combos) {
            if (trySend(combo)) {
                logger.info("Раскладка переключена комбинацией " + combo.name());
                return true;
            }
            logger.warning("Комбинация " + combo.name() + " не сработала, пробуем следующую");
        }
        logger.severe("Ни одна комбинация не переключила раскладку");
        return false;
    }

    private boolean trySend(HotKeyCombo combo) {
        int expected = combo.keys().size() * 2;
        try {
            int sent = keyboard.sendKeyCombo(combo.keys());
            if (sent != expected) {
                logger.warning("Комбинация " + combo.name() + ": принято " + sent + " из " + expected + " событий");
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            logger.severe("Ошибка отправки комбинации " + combo.name() + ": " + e.getMessage());
            return false;
        }
    }
}
