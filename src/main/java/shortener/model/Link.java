package shortener.model;

import java.time.Instant;
import java.util.UUID;

public class Link {
    private final String originalUrl;
    private final String shortUrl;
    private final UUID ownerId;

    private int maxClicks;
    private int currentClicks;

    private final Instant createdAt;
    private final Instant expiresAt;

    public Link(String originalUrl, String shortUrl, UUID ownerId, int maxClicks,
                Instant createdAt, Instant expiresAt) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.ownerId = ownerId;
        this.maxClicks = maxClicks;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.currentClicks = 0;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public int getCurrentClicks() {
        return currentClicks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isLimitReached() {
        return currentClicks >= maxClicks;
    }

    public void incrementClicks() {
        currentClicks++;
    }

    public void updateMaxClicks(UUID actorId, int newLimit) {
        if (!ownerId.equals(actorId)) {
            throw new IllegalStateException("Нет прав: вы не владелец ссылки.");
        }
        if (newLimit < 0) {
            throw new IllegalArgumentException("Лимит должен быть >= 0.");
        }
        this.maxClicks = newLimit;
    }

    @Override
    public String toString() {
        return  shortUrl + " → " + originalUrl + " | Клики: " + currentClicks + "/" + maxClicks +
                " | Действует до: " + expiresAt;
    }
}
