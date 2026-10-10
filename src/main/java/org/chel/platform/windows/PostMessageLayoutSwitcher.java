package org.chel.platform.windows;

import org.chel.core.LayoutSwitcher;
import org.chel.core.Sleeper;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * Переключает раскладку системным сообщением WM_INPUTLANGCHANGEREQUEST.
 * Никакого синтетического ввода — Caps Lock и прочие клавиши не задействованы,
 * поэтому переключение мгновенное и ничего не «залипает».
 * <p>
 * Успех проверяется по-настоящему: сравнением HKL потока до и после. Сообщение
 * доставляется асинхронно, поэтому результат опрашивается короткими интервалами.
 */
public final class PostMessageLayoutSwitcher implements LayoutSwitcher {
    private static final Logger logger = Logger.getLogger(PostMessageLayoutSwitcher.class.getName());
    private static final Duration POLL_INTERVAL = Duration.ofMillis(5);
    private static final int MAX_POLLS = 12; // ~60 мс

    private final Win32Layout layout;
    private final Sleeper sleeper;
    private final int maxPolls;

    public PostMessageLayoutSwitcher(Win32Layout layout) {
        this(layout, Sleeper.REAL, MAX_POLLS);
    }

    PostMessageLayoutSwitcher(Win32Layout layout, Sleeper sleeper, int maxPolls) {
        this.layout = layout;
        this.sleeper = sleeper;
        this.maxPolls = maxPolls;
    }

    @Override
    public boolean switchToNextLayout() {
        Win32Layout.FocusTarget target = layout.focus();
        if (target == null || target.window() == 0) {
            logger.fine("Активного окна нет, системное сообщение отправить некому");
            return false;
        }

        long before = layout.currentLayout(target.threadId());
        layout.requestNextLayout(target.window());
        return awaitChange(target.threadId(), before);
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
                logger.fine("Раскладка переключена системным сообщением");
                return true;
            }
        }
        logger.fine("Окно проигнорировало WM_INPUTLANGCHANGEREQUEST");
        return false;
    }
}
