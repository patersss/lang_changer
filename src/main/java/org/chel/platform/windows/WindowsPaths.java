package org.chel.platform.windows;

import org.chel.core.AppPaths;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * %LOCALAPPDATA%/Capsy с откатом на ~/AppData/Local/Capsy, если переменная не задана.
 */
public final class WindowsPaths implements AppPaths {
    static final String APP_DIRECTORY_NAME = "Capsy";

    private final Function<String, String> environment;
    private final String userHome;

    public WindowsPaths() {
        this(System::getenv, System.getProperty("user.home"));
    }

    WindowsPaths(Function<String, String> environment, String userHome) {
        this.environment = environment;
        this.userHome = userHome;
    }

    @Override
    public Path appDataDirectory() {
        String localAppData = environment.apply("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, APP_DIRECTORY_NAME);
        }
        return Paths.get(userHome, "AppData", "Local", APP_DIRECTORY_NAME);
    }
}
