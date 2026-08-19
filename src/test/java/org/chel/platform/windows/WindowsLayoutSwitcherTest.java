package org.chel.platform.windows;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WindowsLayoutSwitcherTest {

    @Test
    void sendsFirstComboAndStopsWhenItSucceeds() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        WindowsLayoutSwitcher switcher = new WindowsLayoutSwitcher(keyboard);

        assertTrue(switcher.switchToNextLayout());
        assertEquals(1, keyboard.sentCombos().size(), "остальные комбинации пробовать не нужно");
        assertEquals(HotKeyCombo.ALT_SHIFT.keys(), keyboard.sentCombos().getFirst());
    }

    @Test
    void fallsBackToNextComboWhenSystemRejectsEvents() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        keyboard.acceptOnly(0);
        WindowsLayoutSwitcher switcher = new WindowsLayoutSwitcher(keyboard);

        assertFalse(switcher.switchToNextLayout());
        assertEquals(
                List.of(HotKeyCombo.ALT_SHIFT.keys(), HotKeyCombo.WIN_SPACE.keys(), HotKeyCombo.CTRL_SHIFT.keys()),
                keyboard.sentCombos(),
                "должны быть перебраны все комбинации по порядку");
    }

    @Test
    void treatsPartiallyAcceptedComboAsFailure() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        keyboard.acceptOnly(3); // для комбинации из 2 клавиш ожидается 4 события
        WindowsLayoutSwitcher switcher = new WindowsLayoutSwitcher(keyboard, List.of(HotKeyCombo.ALT_SHIFT));

        assertFalse(switcher.switchToNextLayout());
    }

    @Test
    void survivesNativeExceptionAndTriesNextCombo() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        keyboard.failWith(new IllegalStateException("SendInput заблокирован UIPI"));
        WindowsLayoutSwitcher switcher = new WindowsLayoutSwitcher(keyboard);

        assertFalse(assertDoesNotThrow(switcher::switchToNextLayout));
        assertEquals(3, keyboard.sentCombos().size());
    }

    @Test
    void hotKeyComboRejectsEmptyKeyList() {
        assertThrows(IllegalArgumentException.class, () -> new HotKeyCombo("empty", List.of()));
    }
}
