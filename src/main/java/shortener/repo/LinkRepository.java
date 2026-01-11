package shortener.repo;

import shortener.model.Link;

import java.time.Instant;
import java.util.*;

public interface LinkRepository {
    void save(Link link);
    Optional<Link> findByShortUrl(String shortUrl);
    List<Link> findByOwner(UUID ownerId);
    void delete(String shortUrl);

    boolean exists(String shortUrl);
    List<Link> findExpired(Instant now);
}
