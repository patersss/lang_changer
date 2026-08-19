package org.chel.core;

/**
 * Набор платформенно-зависимых сервисов, собранный для текущей ОС.
 * Всё остальное приложение работает только с этими интерфейсами.
 */
public record AppContext(
        String platformName,
        LayoutSwitcher layoutSwitcher,
        CapsLockController capsLockController,
        AppPaths appPaths
) {
}
