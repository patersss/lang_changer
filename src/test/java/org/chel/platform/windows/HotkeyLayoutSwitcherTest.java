package org.chel.platform.windows;

import org.chel.core.Sleeper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HotkeyLayoutSwitcherTest {

    private static final Sleeper NO_WAIT = _ -> {
    };

    private final FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
    private final FakeWin32Layout layout = new FakeWin32Layout();

    private HotkeyLayoutSwitcher switcher(List<HotKeyCombo> combos) {
        return new HotkeyLayoutSwitcher(keyboard, layout, combos, NO_WAIT, 3);
    }

    @Test
    void stopsAtTheFirstComboThatActuallyChangesLayout() {
        keyboard.onSend(layout::applyChange);

        assertTrue(switcher(HotKeyCombo.defaults()).switchToNextLayout());
        assertEquals(1, keyboard.sentCombos().size());
        assertEquals(HotKeyCombo.ALT_SHIFT.keys(), keyboard.sentCombos().getFirst());
    }

    @Test
    void fallsThroughToNextComboWhenLayoutDoesNotChange() {
        int[] sendCount = {0};
        keyboard.onSend(() -> {
            if (++sendCount[0] == 2) {   // срабатывает только вторая комбинация
                layout.applyChange();
            }
        });

        assertTrue(switcher(HotKeyCombo.defaults()).switchToNextLayout());
        assertEquals(
                List.of(HotKeyCombo.ALT_SHIFT.keys(), HotKeyCombo.WIN_SPACE.keys()),
                keyboard.sentCombos(),
                "перебор должен реально доходить до второй комбинации");
    }

    @Test
    void reportsFailureWhenNoComboChangesLayout() {
        assertFalse(switcher(HotKeyCombo.defaults()).switchToNextLayout());
        assertEquals(3, keyboard.sentCombos().size(), "должны быть испробованы все комбинации");
    }

    @Test
    void skipsComboWhenSystemRejectsEvents() {
        keyboard.acceptOnly(0);

        assertFalse(switcher(List.of(HotKeyCombo.ALT_SHIFT)).switchToNextLayout());
    }

    @Test
    void survivesNativeException() {
        keyboard.failWith(new IllegalStateException("SendInput заблокирован UIPI"));

        assertFalse(assertDoesNotThrow(() -> switcher(HotKeyCombo.defaults()).switchToNextLayout()));
    }

    @Test
    void doesNothingWhenNoWindowIsActive() {
        layout.noFocus();

        assertFalse(switcher(HotKeyCombo.defaults()).switchToNextLayout());
        assertTrue(keyboard.sentCombos().isEmpty(), "без активного окна проверять результат не по чему");
    }
}
