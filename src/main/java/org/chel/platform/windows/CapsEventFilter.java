package org.chel.platform.windows;

import org.chel.core.HotkeyListener;
import org.chel.core.Key;
import org.chel.core.KeyStroke;

import java.util.logging.Logger;

/**
 * Состояние одного удержания Caps Lock: решает, поглощать ли событие.
 * <p>
 * Вынесено из хука, потому что здесь два неочевидных правила, и оба ломают
 * поведение незаметно:
 * <ul>
 *   <li>отпускание надо поглощать ровно тогда, когда поглотили нажатие,
 *       иначе приложение получит key-up без key-down;</li>
 *   <li>автоповтор при удержании не должен переключать язык пачкой.</li>
 * </ul>
 */
final class CapsEventFilter {
    private static final Logger logger = Logger.getLogger(CapsEventFilter.class.getName());

    private final HotkeyListener listener;
    private boolean held = false;
    private boolean consumed = false;

    CapsEventFilter(HotkeyListener listener) {
        this.listener = listener;
    }

    /**
     * @return true, если событие нужно поглотить
     */
    boolean shouldSuppress(boolean pressed, boolean shiftDown, boolean injected) {
        if (!pressed) {
            boolean wasConsumed = consumed;
            held = false;
            consumed = false;
            return wasConsumed;
        }

        if (held) {
            return consumed;
        }
        held = true;

        try {
            consumed = listener.onKeyStroke(new KeyStroke(Key.CAPS_LOCK, true, shiftDown, injected));
        } catch (RuntimeException e) {
            logger.severe("Обработчик клавиши упал: " + e.getMessage());
            consumed = false;
        }
        return consumed;
    }
}
