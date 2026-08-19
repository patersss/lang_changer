package org.chel.platform;

import org.chel.platform.windows.WindowsPlatformSupport;

import java.util.List;
import java.util.function.Supplier;

/**
 * Выбирает первую подходящую платформу. Реализации создаются лениво,
 * поэтому классы чужих ОС не загружаются до проверки {@link PlatformSupport#isAvailable()}.
 */
public final class PlatformDetector {
    private static final List<Supplier<PlatformSupport>> CANDIDATES = List.of(
            WindowsPlatformSupport::new
    );

    private PlatformDetector() {
    }

    public static PlatformSupport detect() {
        return detectAmong(CANDIDATES);
    }

    static PlatformSupport detectAmong(List<Supplier<PlatformSupport>> candidates) {
        for (Supplier<PlatformSupport> candidate : candidates) {
            PlatformSupport support = candidate.get();
            if (support.isAvailable()) {
                return support;
            }
        }
        throw new UnsupportedPlatformException(System.getProperty("os.name"));
    }

    public static class UnsupportedPlatformException extends RuntimeException {
        public UnsupportedPlatformException(String osName) {
            super("Платформа не поддерживается: " + osName);
        }
    }
}
