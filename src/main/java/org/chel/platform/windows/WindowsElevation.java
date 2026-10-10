package org.chel.platform.windows;

import org.chel.platform.windows.jna.Shell32Ex;

import java.util.logging.Logger;

/**
 * Проверка того, что процесс запущен с повышенными правами.
 * <p>
 * Это не косметика: UIPI не даёт процессу с обычными правами ни перехватывать
 * клавиши, уходящие в окно с повышенными правами, ни отправлять ему сообщения.
 * Без повышения приложение молча не работает поверх консоли администратора,
 * диспетчера задач, окон UAC и части системных диалогов.
 */
public final class WindowsElevation {
    private static final Logger logger = Logger.getLogger(WindowsElevation.class.getName());

    private WindowsElevation() {
    }

    public static boolean isElevated() {
        try {
            return Shell32Ex.Holder.INSTANCE.IsUserAnAdmin();
        } catch (Throwable e) {
            logger.warning("Не удалось определить уровень прав: " + e.getMessage());
            return false;
        }
    }
}
