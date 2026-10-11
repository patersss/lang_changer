package org.chel.ui;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public final class AppInfo {
    private static final Logger logger = Logger.getLogger(AppInfo.class.getName());
    public static final String version = load();

    private static String load() {
        try(var in = AppInfo.class.getResourceAsStream("/app.properties")) {
            if (in == null) return "dev";

            var props = new Properties();

            props.load(in);
            var version = props.getProperty("app.version", "dev");
            return version.startsWith("${") ? "dev" : version;
        } catch (IOException e) {
            logger.warning("Не смогли получить версию приложения, использует dev: " + e.getMessage());
            return "dev";
        }
    }
}
