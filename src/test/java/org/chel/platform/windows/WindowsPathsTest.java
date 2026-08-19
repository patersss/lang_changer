package org.chel.platform.windows;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowsPathsTest {

    @Test
    void usesLocalAppDataWhenDefined() {
        WindowsPaths paths = new WindowsPaths(
                Map.of("LOCALAPPDATA", "C:\\Users\\tester\\AppData\\Local")::get,
                "C:\\Users\\tester");

        assertEquals(Paths.get("C:\\Users\\tester\\AppData\\Local", "Capsy"), paths.appDataDirectory());
    }

    @Test
    void fallsBackToUserHomeWhenLocalAppDataMissing() {
        WindowsPaths paths = new WindowsPaths(_ -> null, "C:\\Users\\tester");

        assertEquals(Paths.get("C:\\Users\\tester", "AppData", "Local", "Capsy"), paths.appDataDirectory());
    }

    @Test
    void fallsBackToUserHomeWhenLocalAppDataIsBlank() {
        WindowsPaths paths = new WindowsPaths(_ -> "   ", "C:\\Users\\tester");

        Path expected = Paths.get("C:\\Users\\tester", "AppData", "Local", "Capsy");
        assertEquals(expected, paths.appDataDirectory());
    }
}
