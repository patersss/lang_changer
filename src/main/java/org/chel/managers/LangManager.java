package org.chel.managers;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser;
import org.chel.jna.User32Ex;

import java.util.logging.Logger;

import static com.sun.jna.platform.win32.WinUser.INPUT.INPUT_KEYBOARD;
import static com.sun.jna.platform.win32.WinUser.KEYBDINPUT.KEYEVENTF_KEYUP;

public class LangManager {
    private static final Logger logger = Logger.getLogger(LangManager.class.getName());
    private static final int VK_ALT = 0x12;
    private static final int VK_LSHIFT = 0xA0;

    public static void changeLanguage() {
        logger.info("Changing language using alt+shift combo");
        sendCombo(VK_ALT, VK_LSHIFT);
        logger.info("Successfully changed language to next one");
    }
    public static void sendCombo(int key1Vk, int key2Vk) {
        WinUser.INPUT[] inputs = (WinUser.INPUT[]) new WinUser.INPUT().toArray(4);

        // alt DOWN
        inputs[0].type = new DWORD(INPUT_KEYBOARD);
        inputs[0].input.setType("ki");
        inputs[0].input.ki = makeKeyEvent(key1Vk, 0);

        // shift DOWN
        inputs[1].type = new DWORD(INPUT_KEYBOARD);
        inputs[1].input.setType("ki");
        inputs[1].input.ki = makeKeyEvent(key2Vk, 0);

        // shift UP
        inputs[2].type = new DWORD(INPUT_KEYBOARD);
        inputs[2].input.setType("ki");
        inputs[2].input.ki = makeKeyEvent(key2Vk, KEYEVENTF_KEYUP);

        // alt UP
        inputs[3].type = new DWORD(INPUT_KEYBOARD);
        inputs[3].input.setType("ki");
        inputs[3].input.ki = makeKeyEvent(key1Vk, WinUser.KEYBDINPUT.KEYEVENTF_KEYUP);

        User32.INSTANCE.SendInput(new DWORD(inputs.length), inputs, inputs[0].size());
    }

    private static WinUser.KEYBDINPUT makeKeyEvent(int vk, int flags) {
        WinUser.KEYBDINPUT ki = new WinUser.KEYBDINPUT();
        ki.wVk = new WORD(vk);
        ki.wScan = new WORD(0);
        ki.dwFlags = new DWORD(flags);
        ki.time = new DWORD(0);
        ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
        return ki;
    }
}
