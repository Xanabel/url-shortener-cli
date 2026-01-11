package shortener.service;

import org.junit.jupiter.api.Test;
import shortener.model.Link;
import shortener.repo.InMemoryLinkRepository;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CleanupServiceTest {

    @Test
    void cleanupDeletesExpiredAndNotifiesOwner() {
        InMemoryLinkRepository repo = new InMemoryLinkRepository();
        NotificationService notifier = new NotificationService();

        UUID owner = UUID.randomUUID();

        // expiresAt в прошлом
        Link expired = new Link(
                "https://example.com",
                "short.ly/expired",
                owner,
                10,
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(1)
        );
        repo.save(expired);

        new CleanupService(repo, notifier).run();

        assertTrue(repo.findByShortUrl("short.ly/expired").isEmpty());

        var notifications = notifier.drain(owner);
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).getMessage().contains("TTL"));
    }
}
