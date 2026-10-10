package org.chel.platform.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Shell32Ex extends StdCallLibrary {

    final class Holder {
        public static final Shell32Ex INSTANCE = Native.load(
                "shell32",
                Shell32Ex.class,
                W32APIOptions.DEFAULT_OPTIONS
        );

        private Holder() {
        }
    }

    /**
     * @return true, если токен процесса имеет права администратора (процесс запущен с повышением)
     */
    boolean IsUserAnAdmin();
}
