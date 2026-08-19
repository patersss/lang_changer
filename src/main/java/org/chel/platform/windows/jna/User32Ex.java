package org.chel.platform.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32Ex extends StdCallLibrary {

    /**
     * Ленивая загрузка: {@code Native.load} выполняется только при первом обращении
     * к {@link Holder#INSTANCE}, а не при загрузке интерфейса. Иначе один лишь импорт
     * этого типа уронил бы приложение на не-Windows системе.
     */
    final class Holder {
        public static final User32Ex INSTANCE = Native.load(
                "user32",
                User32Ex.class,
                W32APIOptions.DEFAULT_OPTIONS
        );

        private Holder() {
        }
    }

    /**
     * Младший бит результата — состояние toggle для Caps Lock / Num Lock / Scroll Lock.
     */
    short GetKeyState(int nVirtKey);
}
