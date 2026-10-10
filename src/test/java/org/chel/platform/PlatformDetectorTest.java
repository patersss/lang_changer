package org.chel.platform;

import org.chel.core.AppContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PlatformDetectorTest {

    private record StubPlatform(String name, boolean isAvailable) implements PlatformSupport {
        @Override
        public AppContext createContext() {
            return new AppContext(name, () -> true, null, null, false);
        }
    }

    @Test
    void picksFirstAvailablePlatform() {
        List<Supplier<PlatformSupport>> candidates = List.of(
                () -> new StubPlatform("unavailable", false),
                () -> new StubPlatform("first-match", true),
                () -> new StubPlatform("second-match", true));

        assertEquals("first-match", PlatformDetector.detectAmong(candidates).name());
    }

    @Test
    void throwsWhenNoPlatformMatches() {
        List<Supplier<PlatformSupport>> candidates = List.of(() -> new StubPlatform("nope", false));

        assertThrows(PlatformDetector.UnsupportedPlatformException.class,
                () -> PlatformDetector.detectAmong(candidates));
    }

    @Test
    void doesNotInstantiateCandidatesAfterAMatch() {
        boolean[] laterCandidateCreated = {false};
        List<Supplier<PlatformSupport>> candidates = List.of(
                () -> new StubPlatform("match", true),
                () -> {
                    laterCandidateCreated[0] = true;
                    return new StubPlatform("later", true);
                });

        PlatformDetector.detectAmong(candidates);

        assertFalse(laterCandidateCreated[0],
                "классы чужих платформ не должны загружаться после найденного совпадения");
    }
}
