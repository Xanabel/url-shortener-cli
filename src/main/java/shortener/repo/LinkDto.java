package shortener.repo;

import java.time.Instant;
import java.util.UUID;

public class LinkDto {
    public String originalUrl;
    public String shortUrl;
    public UUID ownerId;
    public int maxClicks;
    public int currentClicks;
    public Instant createdAt;
    public Instant expiresAt;

    public LinkDto() {}
}
