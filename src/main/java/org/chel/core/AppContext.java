package org.chel.core;

/**
 * Набор платформенно-зависимых сервисов, собранный для текущей ОС.
 * Всё остальное приложение работает только с этими интерфейсами.
 *
 * @param hasFullPrivileges есть ли у процесса права, достаточные для работы
 *                          поверх окон с повышенными правами
 */
public record AppContext(
        String platformName,
        LayoutSwitcher layoutSwitcher,
        KeyboardHook keyboardHook,
        AppPaths appPaths,
        boolean hasFullPrivileges
) {
}
