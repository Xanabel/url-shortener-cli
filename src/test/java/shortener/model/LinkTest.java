package shortener.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkTest {

    @Test
    void limitReachedWorks() {
        UUID owner = UUID.randomUUID();
        Link link = new Link(
                "https://example.com",
                "short.ly/abc",
                owner,
                2,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        assertFalse(link.isLimitReached());
        link.incrementClicks();
        assertFalse(link.isLimitReached());
        link.incrementClicks();
        assertTrue(link.isLimitReached());
    }

    @Test
    void updateMaxClicksOnlyOwner() {
        UUID owner = UUID.randomUUID();
        UUID чужой = UUID.randomUUID();

        Link link = new Link(
                "https://example.com",
                "short.ly/abc",
                owner,
                10,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        assertThrows(IllegalStateException.class, () -> link.updateMaxClicks(чужой, 5));
        assertDoesNotThrow(() -> link.updateMaxClicks(owner, 5));
        assertEquals(5, link.getMaxClicks());
    }

    @Test
    void updateMaxClicksRejectsNegative() {
        UUID owner = UUID.randomUUID();

        Link link = new Link(
                "https://example.com",
                "short.ly/abc",
                owner,
                10,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        assertThrows(IllegalArgumentException.class, () -> link.updateMaxClicks(owner, -1));
    }
}
