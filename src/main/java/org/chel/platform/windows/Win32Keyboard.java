package org.chel.platform.windows;

import java.util.List;

/**
 * Тонкая обёртка над клавиатурными вызовами user32. Существует ради одного шва:
 * вся остальная Windows-логика оперирует int-кодами и тестируется без нативных вызовов.
 */
public interface Win32Keyboard {
    /**
     * Нажимает клавиши по порядку и отпускает в обратном порядке.
     *
     * @return количество событий, принятых системой (ожидается {@code virtualKeys.size() * 2})
     */
    int sendKeyCombo(List<Integer> virtualKeys);

    /**
     * @return состояние переключателя (Caps Lock, Num Lock и т.п.)
     */
    boolean isToggleOn(int virtualKey);
}
