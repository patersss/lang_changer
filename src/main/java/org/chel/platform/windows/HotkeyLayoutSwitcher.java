package org.chel.platform.windows;

import org.chel.core.LayoutSwitcher;
import org.chel.core.Sleeper;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Запасной путь: эмуляция системной горячей клавиши через SendInput.
 * Нужен для приложений, игнорирующих WM_INPUTLANGCHANGEREQUEST — Electron,
 * UWP, часть Qt.
 * <p>
 * Успех определяется сравнением HKL до и после, а не числом принятых событий:
 * SendInput сообщает лишь о том, что система приняла ввод, поэтому по его
 * результату перебор комбинаций никогда не доходил до второй.
 */
public final class HotkeyLayoutSwitcher implements LayoutSwitcher {
    private static final Logger logger = Logger.getLogger(HotkeyLayoutSwitcher.class.getName());
    private static final Duration POLL_INTERVAL = Duration.ofMillis(10);
    private static final int MAX_POLLS = 8; // ~80 мс на комбинацию

    private final Win32Keyboard keyboard;
    private final Win32Layout layout;
    private final List<HotKeyCombo> combos;
    private final Sleeper sleeper;
    private final int maxPolls;

    public HotkeyLayoutSwitcher(Win32Keyboard keyboard, Win32Layout layout) {
        this(keyboard, layout, HotKeyCombo.defaults(), Sleeper.REAL, MAX_POLLS);
    }

    HotkeyLayoutSwitcher(Win32Keyboard keyboard,
                         Win32Layout layout,
                         List<HotKeyCombo> combos,
                         Sleeper sleeper,
                         int maxPolls) {
        this.keyboard = keyboard;
        this.layout = layout;
        this.combos = List.copyOf(combos);
        this.sleeper = sleeper;
        this.maxPolls = maxPolls;
    }

    @Override
    public boolean switchToNextLayout() {
        Win32Layout.FocusTarget target = layout.focus();
        if (target == null) {
            logger.fine("Активного окна нет, проверить смену раскладки не по чему");
            return false;
        }

        for (HotKeyCombo combo : combos) {
            long before = layout.currentLayout(target.threadId());
            if (!send(combo)) {
                continue;
            }
            if (awaitChange(target.threadId(), before)) {
                logger.info("Раскладка переключена комбинацией " + combo.name());
                return true;
            }
            logger.warning("Комбинация " + combo.name() + " не сменила раскладку, пробуем следующую");
        }
        return false;
    }

    private boolean send(HotKeyCombo combo) {
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

    private boolean awaitChange(int threadId, long before) {
        for (int i = 0; i < maxPolls; i++) {
            try {
                sleeper.sleep(POLL_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            long now = layout.currentLayout(threadId);
            if (now != 0 && now != before) {
                return true;
            }
        }
        return false;
    }
}
