package org.chel.platform.windows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowsPlatformSupportTest {

    @ParameterizedTest
    @ValueSource(strings = {"Windows 10", "Windows 11", "windows server 2022"})
    void detectsWindows(String osName) {
        assertTrue(new WindowsPlatformSupport(() -> osName).isAvailable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Linux", "Mac OS X", "FreeBSD", ""})
    void rejectsOtherPlatforms(String osName) {
        assertFalse(new WindowsPlatformSupport(() -> osName).isAvailable());
    }

    @Test
    void availabilityCheckDoesNotTouchNativeLibraries() {
        // если бы проверка грузила user32, этот вызов упал бы на не-Windows CI
        new WindowsPlatformSupport(() -> "Linux").isAvailable();
    }
}
