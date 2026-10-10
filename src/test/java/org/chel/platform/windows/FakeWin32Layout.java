package org.chel.platform.windows;

import java.util.ArrayList;
import java.util.List;

/**
 * Фейковая раскладка: помнит, каким окнам отправляли запрос, и позволяет
 * задать, сменится ли HKL и с какой попытки.
 */
class FakeWin32Layout implements Win32Layout {
    static final int THREAD_ID = 4242;
    static final long WINDOW = 0xDEADBEEFL;
    static final long EN = 0x04090409L;
    static final long RU = 0x04190419L;

    private final List<Long> requestedWindows = new ArrayList<>();

    private FocusTarget focus = new FocusTarget(THREAD_ID, WINDOW);
    private long layout = EN;
    /** сколько опросов должно пройти, прежде чем раскладка «сменится»; -1 — не сменится никогда */
    private int pollsUntilChange = -1;
    private int polls = 0;

    List<Long> requestedWindows() {
        return requestedWindows;
    }

    void noFocus() {
        this.focus = null;
    }

    void focus(FocusTarget target) {
        this.focus = target;
    }

    void changeAfterPolls(int polls) {
        this.pollsUntilChange = polls;
    }

    /** имитирует смену раскладки прямо сейчас (для проверки запасной стратегии) */
    void applyChange() {
        this.layout = layout == EN ? RU : EN;
    }

    @Override
    public FocusTarget focus() {
        return focus;
    }

    @Override
    public long currentLayout(int threadId) {
        if (pollsUntilChange >= 0 && ++polls > pollsUntilChange) {
            layout = layout == EN ? RU : EN;
            pollsUntilChange = -1;
        }
        return layout;
    }

    @Override
    public void requestNextLayout(long window) {
        requestedWindows.add(window);
    }
}
