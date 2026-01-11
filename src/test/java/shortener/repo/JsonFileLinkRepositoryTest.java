package shortener.repo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shortener.model.Link;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileLinkRepositoryTest {

    @Test
    void savesAndLoadsFromDisk(@TempDir Path dir) {
        Path file = dir.resolve("links.json");

        UUID owner = UUID.randomUUID();
        Link link = new Link(
                "https://example.com",
                "short.ly/abc",
                owner,
                10,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        JsonFileLinkRepository repo1 = new JsonFileLinkRepository(file.toString());
        repo1.save(link);

        // "перезапуск" репозитория
        JsonFileLinkRepository repo2 = new JsonFileLinkRepository(file.toString());
        var loaded = repo2.findByShortUrl("short.ly/abc").orElseThrow();

        assertEquals("https://example.com", loaded.getOriginalUrl());
        assertEquals(owner, loaded.getOwnerId());
        assertEquals(10, loaded.getMaxClicks());
    }

    @Test
    void findByOwnerAndFindExpiredWork(@TempDir Path dir) {
        Path file = dir.resolve("links.json");
        JsonFileLinkRepository repo = new JsonFileLinkRepository(file.toString());

        UUID owner1 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();

        repo.save(new Link("https://a.com", "short.ly/a", owner1, 1,
                Instant.now().minusSeconds(10), Instant.now().minusSeconds(1))); // expired
        repo.save(new Link("https://b.com", "short.ly/b", owner1, 1,
                Instant.now(), Instant.now().plusSeconds(3600))); // active
        repo.save(new Link("https://c.com", "short.ly/c", owner2, 1,
                Instant.now(), Instant.now().plusSeconds(3600))); // active

        assertEquals(2, repo.findByOwner(owner1).size());
        assertEquals(1, repo.findByOwner(owner2).size());

        var expired = repo.findExpired(Instant.now());
        assertEquals(1, expired.size());
        assertEquals("short.ly/a", expired.get(0).getShortUrl());
    }
}

