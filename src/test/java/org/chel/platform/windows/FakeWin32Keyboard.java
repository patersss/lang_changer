package org.chel.platform.windows;

import java.util.ArrayList;
import java.util.List;

/**
 * Подменяет SendInput: запоминает отправленные комбинации и позволяет
 * задать, сколько событий «примет» система.
 */
class FakeWin32Keyboard implements Win32Keyboard {
    private final List<List<Integer>> sentCombos = new ArrayList<>();

    private Integer forcedSentCount;
    private RuntimeException failure;
    private Runnable onSend = () -> {
    };

    List<List<Integer>> sentCombos() {
        return sentCombos;
    }

    void failWith(RuntimeException failure) {
        this.failure = failure;
    }

    void acceptOnly(int eventCount) {
        this.forcedSentCount = eventCount;
    }

    /** позволяет фейковой раскладке «отреагировать» на отправку комбинации */
    void onSend(Runnable onSend) {
        this.onSend = onSend;
    }

    @Override
    public int sendKeyCombo(List<Integer> virtualKeys) {
        sentCombos.add(List.copyOf(virtualKeys));
        onSend.run();
        if (failure != null) {
            throw failure;
        }
        return forcedSentCount != null ? forcedSentCount : virtualKeys.size() * 2;
    }
}
