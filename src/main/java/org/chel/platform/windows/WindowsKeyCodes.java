package org.chel.platform.windows;

/**
 * Virtual-Key коды Windows. Деталь реализации Windows-платформы —
 * за пределы этого пакета выходить не должны.
 */
public final class WindowsKeyCodes {
    public static final int VK_ALT = 0x12;
    public static final int VK_LSHIFT = 0xA0;
    public static final int VK_LWIN = 0x5B;
    public static final int VK_SPACE = 0x20;
    public static final int VK_LCONTROL = 0xA2;
    public static final int VK_CAPITAL = 0x14;

    private WindowsKeyCodes() {
    }
}
