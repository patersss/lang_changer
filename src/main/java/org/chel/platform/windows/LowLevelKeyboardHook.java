package org.chel.platform.windows;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import org.chel.core.HotkeyListener;
import org.chel.core.KeyboardHook;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.chel.platform.windows.WindowsKeyCodes.*;

/**
 * Перехватчик на WH_KEYBOARD_LL. В отличие от jnativehook умеет <b>поглощать</b>
 * событие (возврат 1), поэтому Caps Lock не переключается вовсе — не нужно ни
 * гасить его постфактум, ни ждать, ни бороться с миганием.
 * <p>
 * Хук глобальный, но его колбэки доставляются через очередь сообщений потока,
 * который его установил, поэтому здесь свой поток с циклом GetMessage.
 */
public final class LowLevelKeyboardHook implements KeyboardHook {
    private static final Logger logger = Logger.getLogger(LowLevelKeyboardHook.class.getName());
    private static final LRESULT SUPPRESS = new LRESULT(1);

    private final User32 user32;

    /**
     * Сильная ссылка на колбэк обязательна: JNA не удерживает его сама,
     * и после сборки мусора процесс упадёт в нативном коде.
     */
    private LowLevelKeyboardProc callback;
    private volatile HHOOK hook;
    private volatile int hookThreadId;
    private Thread hookThread;
    private CapsEventFilter filter;

    public LowLevelKeyboardHook() {
        this(User32.INSTANCE);
    }

    LowLevelKeyboardHook(User32 user32) {
        this.user32 = user32;
    }

    @Override
    public void start(HotkeyListener listener) {
        this.filter = new CapsEventFilter(listener);
        this.callback = this::onKeyEvent;

        CountDownLatch installed = new CountDownLatch(1);
        hookThread = new Thread(() -> runMessageLoop(installed), "Keyboard-Hook");
        hookThread.setDaemon(true);
        hookThread.start();

        try {
            if (!installed.await(5, TimeUnit.SECONDS)) {
                logger.severe("Хук не установился за 5 секунд");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void runMessageLoop(CountDownLatch installed) {
        hookThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
        hook = user32.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, callback,
                Kernel32.INSTANCE.GetModuleHandle(null), 0);

        if (hook == null) {
            logger.severe("SetWindowsHookEx не удался, код " + Kernel32.INSTANCE.GetLastError());
            installed.countDown();
            return;
        }
        logger.info("Низкоуровневый хук клавиатуры установлен");
        installed.countDown();

        MSG msg = new MSG();
        while (user32.GetMessage(msg, null, 0, 0) > 0) {
            user32.TranslateMessage(msg);
            user32.DispatchMessage(msg);
        }

        user32.UnhookWindowsHookEx(hook);
        hook = null;
        logger.info("Низкоуровневый хук клавиатуры снят");
    }

    /**
     * Горячий путь: вызывается на КАЖДОЕ нажатие в системе, поэтому всё
     * неинтересное отсеивается до любых аллокаций.
     */
    private LRESULT onKeyEvent(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
        if (nCode < 0 || info.vkCode != VK_CAPITAL) {
            return passThrough(nCode, wParam, info);
        }

        int message = wParam.intValue();
        boolean pressed = message == WM_KEYDOWN || message == WM_SYSKEYDOWN;
        boolean released = message == WM_KEYUP || message == WM_SYSKEYUP;
        if (!pressed && !released) {
            return passThrough(nCode, wParam, info);
        }

        boolean suppress = filter.shouldSuppress(
                pressed,
                (user32.GetAsyncKeyState(VK_SHIFT) & KEY_PRESSED_MASK) != 0,
                (info.flags & LLKHF_INJECTED) != 0);

        return suppress ? SUPPRESS : passThrough(nCode, wParam, info);
    }

    private LRESULT passThrough(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
        return user32.CallNextHookEx(null, nCode, wParam,
                new LPARAM(Pointer.nativeValue(info.getPointer())));
    }

    @Override
    public void close() {
        int threadId = hookThreadId;
        if (threadId == 0) {
            return;
        }
        user32.PostThreadMessage(threadId, WM_QUIT, new WPARAM(0), new LPARAM(0));
        try {
            hookThread.join(TimeUnit.SECONDS.toMillis(2));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        hookThreadId = 0;
        callback = null;
    }
}
