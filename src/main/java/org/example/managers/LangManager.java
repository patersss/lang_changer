package org.example.managers;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HKL;
import org.example.jna.User32Ex;

import java.util.Arrays;
import java.util.logging.Logger;

public class LangManager {
    private static final Logger logger = Logger.getLogger(LangManager.class.getName());
    private static final User32 user32 = User32.INSTANCE;
    private static final User32Ex user32Ex = User32Ex.INSTANCE;
    private static final int KLF_ACTIVATE = 0x00000001;

    public static void changeLang() {
        try {
            HKL[] layouts = new HKL[10];
            int countOfLangs = user32.GetKeyboardLayoutList(layouts.length, layouts);
            Arrays.stream(layouts).forEach(System.out::println);
            if (countOfLangs == 0) {
                logger.warning("Does not find any keyboard layouts!");
                return;
            }

            WinDef.HWND foregroundWindow = user32.GetForegroundWindow();
            int threadId = user32.GetWindowThreadProcessId(foregroundWindow, null);
            HKL current = user32.GetKeyboardLayout(threadId);

            logger.info("Current layout: " + formatHKL(current));
            logger.info("Total layouts: " + countOfLangs);


            logger.info("Get current lang: " + current);
            for (int i = 0; i < countOfLangs; i++) {
                if (layouts[i].equals(current)) {
                    HKL next = layouts[(i + 1) % countOfLangs];
                    user32Ex.ActivateKeyboardLayout(next, KLF_ACTIVATE);
                    logger.info("Change layout to: " + next.toString());
                    return;
                }
            }

            logger.warning("Current layout was not found. Set the first available");
            user32Ex.ActivateKeyboardLayout(layouts[0], KLF_ACTIVATE);
        } catch (Exception e) {
            logger.severe("Error while changing language: " + e.getMessage());
        }
    }

    private static String formatHKL(HKL hkl) {
        return String.format("0x%08X", hkl.hashCode());
    }
}
