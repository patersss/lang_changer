package org.chel.platform.windows;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WindowsCapsLockControllerTest {

    private WindowsCapsLockController controller(FakeWin32Keyboard keyboard) {
        return new WindowsCapsLockController(keyboard, Duration.ZERO);
    }

    @Test
    void doesNothingWhenCapsLockIsAlreadyOff() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        keyboard.setToggleOn(false);

        controller(keyboard).turnOff();

        assertTrue(keyboard.sentCombos().isEmpty(), "лишнее нажатие включило бы Caps Lock обратно");
    }

    @Test
    void sendsCapsLockKeyOnceWhenToggleClears() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        // 1-й вызов — проверка перед отправкой, 2-й — проверка после
        keyboard.scriptToggleStates(true, false);

        controller(keyboard).turnOff();

        assertEquals(List.of(List.of(WindowsKeyCodes.VK_CAPITAL)), keyboard.sentCombos());
    }

    @Test
    void retriesOnceWhenCapsLockStaysOn() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        keyboard.setToggleOn(true); // всегда включён — обе попытки безуспешны

        controller(keyboard).turnOff();

        assertEquals(2, keyboard.sentCombos().size(), "ожидается ровно одна повторная попытка");
    }

    @Test
    void stopsAfterSuccessfulRetry() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        // проверка перед 1-й отправкой, после 1-й (ещё включён), после 2-й (выключен)
        keyboard.scriptToggleStates(true, true, false);

        controller(keyboard).turnOff();

        assertEquals(2, keyboard.sentCombos().size());
    }

    @Test
    void isOnReadsCapsLockVirtualKey() {
        FakeWin32Keyboard keyboard = new FakeWin32Keyboard();
        keyboard.setToggleOn(true);

        assertTrue(controller(keyboard).isOn());
    }
}
