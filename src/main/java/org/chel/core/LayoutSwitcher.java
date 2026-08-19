package org.chel.core;

/**
 * Переключение раскладки клавиатуры. Реализация зависит от платформы:
 * на Windows это эмуляция горячих клавиш, на X11 — XkbLockGroup.
 */
public interface LayoutSwitcher {
    /**
     * @return true, если переключение удалось выполнить
     */
    boolean switchToNextLayout();
}
