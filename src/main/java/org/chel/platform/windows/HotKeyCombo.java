package org.chel.platform.windows;

import java.util.List;

/**
 * Комбинация Virtual-Key кодов, переключающая раскладку в настройках Windows.
 */
public record HotKeyCombo(String name, List<Integer> keys) {
    public HotKeyCombo {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("Комбинация " + name + " не содержит клавиш");
        }
        keys = List.copyOf(keys);
    }

    public static final HotKeyCombo ALT_SHIFT =
            new HotKeyCombo("alt+shift", List.of(WindowsKeyCodes.VK_ALT, WindowsKeyCodes.VK_LSHIFT));
    public static final HotKeyCombo WIN_SPACE =
            new HotKeyCombo("win+space", List.of(WindowsKeyCodes.VK_LWIN, WindowsKeyCodes.VK_SPACE));
    public static final HotKeyCombo CTRL_SHIFT =
            new HotKeyCombo("ctrl+shift", List.of(WindowsKeyCodes.VK_LCONTROL, WindowsKeyCodes.VK_LSHIFT));

    public static List<HotKeyCombo> defaults() {
        return List.of(ALT_SHIFT, WIN_SPACE, CTRL_SHIFT);
    }
}
