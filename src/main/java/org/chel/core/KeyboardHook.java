package org.chel.core;

/**
 * Глобальный перехватчик клавиатуры, умеющий поглощать события.
 * <p>
 * Поглощение — обязательное свойство: только оно позволяет не дать Windows
 * переключить Caps Lock. Реализуемо и на X11 (XGrabKey), и на evdev/uinput.
 */
public interface KeyboardHook extends AutoCloseable {
    void start(HotkeyListener listener);

    @Override
    void close();
}
