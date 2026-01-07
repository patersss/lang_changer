package org.example.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HKL;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32Ex extends StdCallLibrary {
    User32Ex INSTANCE = Native.load(
            "user32",
            User32Ex.class,
            W32APIOptions.DEFAULT_OPTIONS
    );

    // get state of caps lock
    short GetKeyState(int nVirtKey);

    // get async state of caps lock
    short GetAsyncKeyState(int nVirtKey);

    boolean PostMessage(
            WinDef.HWND hWnd,
            int msg,
            WinDef.WPARAM wParam,
            WinDef.LPARAM lParam
    );

}
