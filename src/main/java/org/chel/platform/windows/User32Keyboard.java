package org.chel.platform.windows;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.platform.win32.WinUser;

import java.util.List;

import static com.sun.jna.platform.win32.WinUser.INPUT.INPUT_KEYBOARD;
import static com.sun.jna.platform.win32.WinUser.KEYBDINPUT.KEYEVENTF_KEYUP;

/**
 * Реализация {@link Win32Keyboard} через SendInput/GetKeyState.
 */
public final class User32Keyboard implements Win32Keyboard {

    @Override
    public int sendKeyCombo(List<Integer> virtualKeys) {
        WinUser.INPUT[] inputs = buildInputs(virtualKeys);
        DWORD sent = User32.INSTANCE.SendInput(new DWORD(inputs.length), inputs, inputs[0].size());
        return sent.intValue();
    }

    private static WinUser.INPUT[] buildInputs(List<Integer> virtualKeys) {
        int total = virtualKeys.size() * 2;
        WinUser.INPUT[] inputs = (WinUser.INPUT[]) new WinUser.INPUT().toArray(total);

        for (int i = 0; i < virtualKeys.size(); i++) {
            fill(inputs[i], virtualKeys.get(i), 0);
        }
        // отпускаем в обратном порядке: последняя нажатая клавиша отпускается первой
        for (int i = 0; i < virtualKeys.size(); i++) {
            int key = virtualKeys.get(virtualKeys.size() - 1 - i);
            fill(inputs[virtualKeys.size() + i], key, KEYEVENTF_KEYUP);
        }
        return inputs;
    }

    private static void fill(WinUser.INPUT input, int virtualKey, int flags) {
        input.type = new DWORD(INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wVk = new WORD(virtualKey);
        input.input.ki.wScan = new WORD(0);
        input.input.ki.dwFlags = new DWORD(flags);
        input.input.ki.time = new DWORD(0);
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
    }
}
