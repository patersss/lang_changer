package org.chel.core;

/**
 * Одно событие клавиши, полученное перехватчиком.
 *
 * @param key      отслеживаемая клавиша
 * @param pressed  true — нажатие, false — отпускание
 * @param shiftDown зажат ли Shift в момент события
 * @param injected  событие сгенерировано программно (нами или другим приложением),
 *                  а не реальным нажатием
 */
public record KeyStroke(Key key, boolean pressed, boolean shiftDown, boolean injected) {
}
