package org.chel.core;

@FunctionalInterface
public interface HotkeyListener {
    /**
     * Вызывается из перехватчика на каждое отслеживаемое событие.
     * <p>
     * ВАЖНО: должен возвращаться немедленно. Windows снимает низкоуровневый хук,
     * если обработчик не уложился в LowLevelHooksTimeout (по умолчанию 300 мс),
     * причём молча — приложение просто перестанет реагировать.
     *
     * @return true, если событие нужно поглотить и не пропускать в систему
     */
    boolean onKeyStroke(KeyStroke stroke);
}
