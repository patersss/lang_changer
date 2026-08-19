package org.chel.core;

import java.nio.file.Path;

/**
 * Расположение пользовательских данных приложения. На каждой ОС свои соглашения:
 * %LOCALAPPDATA% на Windows, $XDG_DATA_HOME на Linux, ~/Library на macOS.
 */
public interface AppPaths {
    Path appDataDirectory();
}
