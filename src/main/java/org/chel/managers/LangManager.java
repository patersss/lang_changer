package org.chel.managers;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.sun.jna.platform.win32.WinUser.INPUT.INPUT_KEYBOARD;
import static com.sun.jna.platform.win32.WinUser.KEYBDINPUT.KEYEVENTF_KEYUP;

public class LangManager {
    private static final Logger logger = Logger.getLogger(LangManager.class.getName());
    private static final List<HotKeyCombo> languageHotkeys = new ArrayList<>();

    static {
        languageHotkeys.add(new HotKeyCombo("alt+shift",
                List.of(KeyConstantsUtils.VK_ALT, KeyConstantsUtils.VK_LSHIFT)));
        languageHotkeys.add(new HotKeyCombo("win+space",
                List.of(KeyConstantsUtils.VK_LWIN, KeyConstantsUtils.VK_SPACE)));
        languageHotkeys.add(new HotKeyCombo("ctrl+shift",
                List.of(KeyConstantsUtils.VK_LCONTROL, KeyConstantsUtils.VK_LSHIFT)));
    }

    public static void changeLanguage() {
        logger.info("Changing language using hotkey");
        for (var keyCombo : languageHotkeys) {
            boolean status = sendCombo(keyCombo);

            if (status) {
                logger.info("Successfully changed language using " + keyCombo.getComboName());
                return;
            }

            logger.warning(keyCombo.getComboName() + " combo didn`t work. Trying another one");
            sleep(50);
        }
        logger.severe("All hotkey combinations failed to change language");
    }

    private static boolean sendCombo(HotKeyCombo combo) {
        logger.info("Trying " + combo.getComboName() + " combo");
        try {
            var inputs = createHotkeyInputs(combo);
            DWORD result = User32.INSTANCE.SendInput(new DWORD(inputs.length), inputs, inputs[0].size());
            int sentEvents = result.intValue();
            boolean success = sentEvents == inputs.length;

            if (!success) {
                logger.warning("Error while sending inputs to change lang. Sent " + combo.getComboName() +
                        " ,only sent " + sentEvents + " of " + inputs.length + " events");
            }

            return success;
        } catch (Exception e) {
            logger.severe("Error while sending inputs to change lang. Use " + combo.getComboName() +
                    " " + e.getMessage());
            return false;
        }
    }

    private static WinUser.INPUT[] createHotkeyInputs(HotKeyCombo combo) {
        var comboKeys = combo.getKeys();
        int doubledSize = comboKeys.size() * 2;
        WinUser.INPUT[] inputs = (WinUser.INPUT[]) new WinUser.INPUT().toArray(doubledSize);

        for (int i = 0; i < comboKeys.size(); i++) {
            inputs[i].type = new DWORD(INPUT_KEYBOARD);
            inputs[i].input.setType("ki");
            inputs[i].input.ki = makeKeyEvent(comboKeys.get(i), 0);
        }

        for (int i = comboKeys.size() - 1; i >= 0; i--) {
            int indexOfUpEvents = doubledSize - 1 - i;
            inputs[indexOfUpEvents].type = new DWORD(INPUT_KEYBOARD);
            inputs[indexOfUpEvents].input.setType("ki");
            inputs[indexOfUpEvents].input.ki = makeKeyEvent(comboKeys.get(i), KEYEVENTF_KEYUP);
        }
        return inputs;
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

    private static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
