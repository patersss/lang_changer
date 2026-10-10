package org.chel.platform.windows;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HKL;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.GUITHREADINFO;

import static org.chel.platform.windows.WindowsKeyCodes.INPUTLANGCHANGE_FORWARD;
import static org.chel.platform.windows.WindowsKeyCodes.WM_INPUTLANGCHANGEREQUEST;

public final class User32Layout implements Win32Layout {
    private final User32 user32;

    public User32Layout() {
        this(User32.INSTANCE);
    }

    User32Layout(User32 user32) {
        this.user32 = user32;
    }

    @Override
    public FocusTarget focus() {
        HWND foreground = user32.GetForegroundWindow();
        if (foreground == null) {
            return null;
        }
        int threadId = user32.GetWindowThreadProcessId(foreground, null);
        if (threadId == 0) {
            return null;
        }

        // в приложениях со сложной иерархией ввод получает дочерний контрол,
        // а не само верхнеуровневое окно
        GUITHREADINFO info = new GUITHREADINFO();
        info.cbSize = info.size();
        HWND target = foreground;
        if (user32.GetGUIThreadInfo(threadId, info) && info.hwndFocus != null) {
            target = info.hwndFocus;
        }
        return new FocusTarget(threadId, handleOf(target));
    }

    @Override
    public long currentLayout(int threadId) {
        HKL layout = user32.GetKeyboardLayout(threadId);
        return layout == null ? 0 : handleOf(layout);
    }

    @Override
    public void requestNextLayout(long window) {
        user32.PostMessage(new HWND(new Pointer(window)),
                WM_INPUTLANGCHANGEREQUEST,
                new WPARAM(INPUTLANGCHANGE_FORWARD),
                new LPARAM(0));
    }

    private static long handleOf(com.sun.jna.platform.win32.WinNT.HANDLE handle) {
        Pointer pointer = handle.getPointer();
        return pointer == null ? 0 : Pointer.nativeValue(pointer);
    }
}
