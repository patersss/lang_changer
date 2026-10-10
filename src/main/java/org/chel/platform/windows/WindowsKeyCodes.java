package org.chel.platform.windows;

/**
 * Константы Windows API. Деталь реализации Windows-платформы —
 * за пределы этого пакета выходить не должны.
 */
public final class WindowsKeyCodes {
    // Virtual-Key коды
    public static final int VK_ALT = 0x12;
    public static final int VK_SHIFT = 0x10;
    public static final int VK_LSHIFT = 0xA0;
    public static final int VK_LWIN = 0x5B;
    public static final int VK_SPACE = 0x20;
    public static final int VK_LCONTROL = 0xA2;
    public static final int VK_CAPITAL = 0x14;

    // Сообщения клавиатуры, приходящие в низкоуровневый хук
    public static final int WM_KEYDOWN = 0x0100;
    public static final int WM_KEYUP = 0x0101;
    public static final int WM_SYSKEYDOWN = 0x0104;
    public static final int WM_SYSKEYUP = 0x0105;
    public static final int WM_QUIT = 0x0012;

    /** Смена раскладки: wParam — флаги, lParam — HKL. */
    public static final int WM_INPUTLANGCHANGEREQUEST = 0x0050;
    public static final int INPUTLANGCHANGE_FORWARD = 0x0002;

    /** KBDLLHOOKSTRUCT.flags: событие сгенерировано программно, а не реальным нажатием. */
    public static final int LLKHF_INJECTED = 0x10;

    /** Старший бит GetAsyncKeyState — клавиша сейчас зажата. */
    public static final int KEY_PRESSED_MASK = 0x8000;

    private WindowsKeyCodes() {
    }
}
