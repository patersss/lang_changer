package org.chel.core;

/**
 * Чтение и сброс состояния Caps Lock.
 */
public interface CapsLockController {
    boolean isOn();

    /**
     * Выключает Caps Lock, если он включён. Вызов при выключенном Caps Lock — no-op.
     */
    void turnOff();
}
