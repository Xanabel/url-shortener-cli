package shortener.repo;

import shortener.model.Link;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLinkRepository implements LinkRepository {
    private final Map<String, Link> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Link link) {
        storage.put(link.getShortUrl(), link);
    }

    @Override
    public Optional<Link> findByShortUrl(String shortUrl) {
        return Optional.ofNullable(storage.get(shortUrl));
    }

    @Override
    public List<Link> findByOwner(UUID ownerId) {
        List<Link> res = new ArrayList<>();
        for (Link l : storage.values()) {
            if (l.getOwnerId().equals(ownerId)) res.add(l);
        }
        return res;
    }

    @Override
    public void delete(String shortUrl) {
        storage.remove(shortUrl);
    }

    @Override
    public boolean exists(String shortUrl) {
        return storage.containsKey(shortUrl);
    }

    @Override
    public List<Link> findExpired(Instant now) {
        List<Link> res = new ArrayList<>();
        for (Link l : storage.values()) {
            if (l.isExpired(now)) res.add(l);
        }
        return res;
    }
}

