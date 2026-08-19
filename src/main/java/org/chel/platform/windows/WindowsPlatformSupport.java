package org.chel.platform.windows;

import org.chel.core.AppContext;
import org.chel.platform.PlatformSupport;

import java.util.Locale;
import java.util.function.Supplier;

public final class WindowsPlatformSupport implements PlatformSupport {
    private final Supplier<String> osNameSupplier;

    public WindowsPlatformSupport() {
        this(() -> System.getProperty("os.name", ""));
    }

    WindowsPlatformSupport(Supplier<String> osNameSupplier) {
        this.osNameSupplier = osNameSupplier;
    }

    @Override
    public String name() {
        return "Windows";
    }

    @Override
    public boolean isAvailable() {
        return osNameSupplier.get().toLowerCase(Locale.ROOT).contains("win");
    }

    @Override
    public AppContext createContext() {
        Win32Keyboard keyboard = new User32Keyboard();
        return new AppContext(
                name(),
                new WindowsLayoutSwitcher(keyboard),
                new WindowsCapsLockController(keyboard),
                new WindowsPaths()
        );
    }
}
