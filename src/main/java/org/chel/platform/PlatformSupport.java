package org.chel.platform;

import org.chel.core.AppContext;

/**
 * Фабрика платформенных реализаций. Добавление новой ОС = новый класс,
 * реализующий этот интерфейс, плюс строка в {@link PlatformDetector}.
 * <p>
 * В сигнатурах не должно быть типов, специфичных для конкретной ОС (JNA, X11 и т.п.),
 * иначе загрузка класса на чужой платформе упадёт.
 */
public interface PlatformSupport {
    String name();

    /**
     * Проверка «мы на этой ОС и она нам подходит». Не должна трогать нативные библиотеки.
     */
    boolean isAvailable();

    AppContext createContext();
}
