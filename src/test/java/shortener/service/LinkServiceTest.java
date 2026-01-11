package shortener.service;

import org.junit.jupiter.api.Test;
import shortener.config.AppConfig;
import shortener.model.Link;
import shortener.repo.InMemoryLinkRepository;
import shortener.repo.LinkRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    private static AppConfig cfg() {
        // baseUrl, ttlHours, defaultMaxClicks, cleanupIntervalSeconds, currentUserFile, storageFile
        return new AppConfig("short.ly", 24, 10, 30, ".u", "x.json");
    }

    @Test
    void createGeneratesShortUrlWithBaseUrl() {
        LinkRepository repo = new InMemoryLinkRepository();
        NotificationService notifier = new NotificationService();
        LinkService svc = new LinkService(repo, notifier, cfg());

        UUID userId = UUID.randomUUID();
        Link link = svc.createLink(userId, "https://example.com", null);

        assertNotNull(link.getShortUrl());
        assertTrue(link.getShortUrl().startsWith("short.ly/"));
        assertEquals(userId, link.getOwnerId());
    }

    @Test
    void createSameOriginalUrlForDifferentUsersGivesDifferentShortUrls() {
        LinkRepository repo = new InMemoryLinkRepository();
        NotificationService notifier = new NotificationService();
        LinkService svc = new LinkService(repo, notifier, cfg());

        Link a = svc.createLink(UUID.randomUUID(), "https://example.com", null);
        Link b = svc.createLink(UUID.randomUUID(), "https://example.com", null);

        assertNotEquals(a.getShortUrl(), b.getShortUrl());
    }

    @Test
    void createRejectsInvalidUrl() {
        LinkRepository repo = new InMemoryLinkRepository();
        NotificationService notifier = new NotificationService();
        LinkService svc = new LinkService(repo, notifier, cfg());

        assertThrows(IllegalArgumentException.class,
                () -> svc.createLink(UUID.randomUUID(), "ftp://example.com", null));
        assertThrows(IllegalArgumentException.class,
                () -> svc.createLink(UUID.randomUUID(), "not-a-url", null));
    }

    @Test
    void limitBlocksAfterReaching() {
        LinkRepository repo = new InMemoryLinkRepository();
        NotificationService notifier = new NotificationService();
        LinkService svc = new LinkService(repo, notifier, cfg());

        UUID owner = UUID.randomUUID();
        Link link = svc.createLink(owner, "https://example.com", 2);

        // Мы не можем надежно проверить Desktop browse в тесте,
        // но можем проверить, что клики считаются и лимит наступает.
        assertEquals(0, link.getCurrentClicks());

        // имитируем открытия напрямую через модель (чтобы не дергать Desktop)
        link.incrementClicks();
        repo.save(link);
        link.incrementClicks();
        repo.save(link);

        assertTrue(link.isLimitReached());
    }

    @Test
    void updateLimitAllowedOnlyForOwner() {
        LinkRepository repo = new InMemoryLinkRepository();
        NotificationService notifier = new NotificationService();
        LinkService svc = new LinkService(repo, notifier, cfg());

        UUID owner = UUID.randomUUID();
        Link link = svc.createLink(owner, "https://example.com", 5);

        UUID чужой = UUID.randomUUID();
        assertThrows(IllegalStateException.class,
                () -> svc.updateLimit(чужой, link.getShortUrl(), 1));

        assertDoesNotThrow(() -> svc.updateLimit(owner, link.getShortUrl(), 1));
        Link updated = repo.findByShortUrl(link.getShortUrl()).orElseThrow();
        assertEquals(1, updated.getMaxClicks());
    }

    @Test
    void deleteAllowedOnlyForOwner() {
        LinkRepository repo = new InMemoryLinkRepository();
        NotificationService notifier = new NotificationService();
        LinkService svc = new LinkService(repo, notifier, cfg());

        UUID owner = UUID.randomUUID();
        Link link = svc.createLink(owner, "https://example.com", 5);

        UUID чужой = UUID.randomUUID();
        assertThrows(IllegalStateException.class,
                () -> svc.delete(чужой, link.getShortUrl()));

        assertDoesNotThrow(() -> svc.delete(owner, link.getShortUrl()));
        assertTrue(repo.findByShortUrl(link.getShortUrl()).isEmpty());
    }
}

