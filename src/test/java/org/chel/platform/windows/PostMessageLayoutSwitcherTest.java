package org.chel.platform.windows;

import org.chel.core.Sleeper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostMessageLayoutSwitcherTest {

    private static final Sleeper NO_WAIT = _ -> {
    };

    private PostMessageLayoutSwitcher switcher(FakeWin32Layout layout) {
        return new PostMessageLayoutSwitcher(layout, NO_WAIT, 5);
    }

    @Test
    void reportsSuccessWhenLayoutActuallyChanges() {
        FakeWin32Layout layout = new FakeWin32Layout();
        layout.changeAfterPolls(2);

        assertTrue(switcher(layout).switchToNextLayout());
    }

    @Test
    void postsToTheFocusedWindow() {
        FakeWin32Layout layout = new FakeWin32Layout();
        layout.changeAfterPolls(1);

        switcher(layout).switchToNextLayout();

        assertEquals(List.of(FakeWin32Layout.WINDOW), layout.requestedWindows());
    }

    @Test
    void reportsFailureWhenWindowIgnoresTheMessage() {
        FakeWin32Layout layout = new FakeWin32Layout(); // раскладка не меняется

        assertFalse(switcher(layout).switchToNextLayout(),
                "Electron и UWP игнорируют сообщение — это должно быть видно вызывающему");
    }

    @Test
    void doesNothingWhenNoWindowIsActive() {
        FakeWin32Layout layout = new FakeWin32Layout();
        layout.noFocus();

        assertFalse(switcher(layout).switchToNextLayout());
        assertTrue(layout.requestedWindows().isEmpty(), "слать сообщение некому");
    }

    @Test
    void doesNothingWhenFocusHasNoWindowHandle() {
        FakeWin32Layout layout = new FakeWin32Layout();
        layout.focus(new Win32Layout.FocusTarget(FakeWin32Layout.THREAD_ID, 0));

        assertFalse(switcher(layout).switchToNextLayout());
        assertTrue(layout.requestedWindows().isEmpty());
    }
}
