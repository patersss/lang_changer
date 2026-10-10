package org.chel.core;

import java.util.List;
import java.util.logging.Logger;

/**
 * Пробует стратегии по порядку и останавливается на первой, которая реально
 * переключила раскладку.
 * <p>
 * Нужна потому, что универсального способа нет: системное сообщение работает
 * мгновенно в большинстве Win32-приложений, но игнорируется частью Electron/UWP/Qt,
 * где выручает только эмуляция горячей клавиши.
 */
public final class FirstWorkingLayoutSwitcher implements LayoutSwitcher {
    private static final Logger logger = Logger.getLogger(FirstWorkingLayoutSwitcher.class.getName());

    private final List<LayoutSwitcher> strategies;

    public FirstWorkingLayoutSwitcher(LayoutSwitcher... strategies) {
        this(List.of(strategies));
    }

    public FirstWorkingLayoutSwitcher(List<LayoutSwitcher> strategies) {
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("Нужна хотя бы одна стратегия переключения");
        }
        this.strategies = List.copyOf(strategies);
    }

    @Override
    public boolean switchToNextLayout() {
        for (LayoutSwitcher strategy : strategies) {
            try {
                if (strategy.switchToNextLayout()) {
                    return true;
                }
                logger.fine(strategy.getClass().getSimpleName() + " не сработал, пробуем следующий");
            } catch (RuntimeException e) {
                logger.warning(strategy.getClass().getSimpleName() + " упал: " + e.getMessage());
            }
        }
        logger.severe("Ни одна стратегия не переключила раскладку");
        return false;
    }
}
