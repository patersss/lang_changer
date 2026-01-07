package org.example.managers;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HKL;
import org.example.jna.User32Ex;

import java.util.logging.Logger;

public class LangManager {
    private static final Logger logger = Logger.getLogger(LangManager.class.getName());

    private static final User32 user32 = User32.INSTANCE;
    private static final User32Ex user32Ex = User32Ex.INSTANCE;
    private static final int WM_INPUTLANGCHANGEREQUEST = 0x0050;
    private static final long HKL_EN = 0x04090409L;
    private static final long HKL_RU = 0x04190419L;

    public static void changeLangTest() {
        logger.info("Changing lang...");
        WinDef.HWND hwnd = user32.GetForegroundWindow();
        if (hwnd == null) {
            logger.warning("Foreground windows is null");
            return;
        }

        int threadId = user32.GetWindowThreadProcessId(hwnd, null);
        HKL current = user32.GetKeyboardLayout(threadId);

        long currentVal = current.hashCode() & 0xFFFFFFFFL;

        long nextVal = (currentVal == HKL_EN) ? HKL_RU : HKL_EN;
        logger.info("Setting lang to " + nextVal);

        user32Ex.PostMessage(
                hwnd,
                WM_INPUTLANGCHANGEREQUEST,
                new WinDef.WPARAM(0),
                new WinDef.LPARAM(nextVal)
        );

        logger.info("Requested layout switch: 0x" + Long.toHexString(nextVal));
    }
}
