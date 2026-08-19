package org.chel.platform.windows;

import java.util.ArrayList;
import java.util.List;

/**
 * Подменяет нативные вызовы user32: запоминает отправленные комбинации
 * и позволяет задать состояние Caps Lock и результат SendInput.
 */
class FakeWin32Keyboard implements Win32Keyboard {
    private final List<List<Integer>> sentCombos = new ArrayList<>();
    private final List<Boolean> toggleStates = new ArrayList<>();

    /** сколько событий «примет» система: null — принять все */
    private Integer forcedSentCount;
    private RuntimeException failure;
    private int toggleReadIndex = 0;
    private boolean toggleOn = false;

    List<List<Integer>> sentCombos() {
        return sentCombos;
    }

    void failWith(RuntimeException failure) {
        this.failure = failure;
    }

    void acceptOnly(int eventCount) {
        this.forcedSentCount = eventCount;
    }

    void setToggleOn(boolean on) {
        this.toggleOn = on;
    }

    /** Задаёт последовательность ответов isToggleOn: по одному на каждый вызов. */
    void scriptToggleStates(Boolean... states) {
        toggleStates.addAll(List.of(states));
    }

    @Override
    public int sendKeyCombo(List<Integer> virtualKeys) {
        sentCombos.add(List.copyOf(virtualKeys));
        if (failure != null) {
            throw failure;
        }
        return forcedSentCount != null ? forcedSentCount : virtualKeys.size() * 2;
    }

    @Override
    public boolean isToggleOn(int virtualKey) {
        if (toggleReadIndex < toggleStates.size()) {
            return toggleStates.get(toggleReadIndex++);
        }
        return toggleOn;
    }
}
