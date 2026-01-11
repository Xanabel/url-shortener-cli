package shortener.service;

import shortener.config.AppConfig;
import shortener.model.Link;
import shortener.repo.LinkRepository;

import java.awt.Desktop;
import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class LinkService {
    private final LinkRepository repo;
    private final NotificationService notifier;
    private final AppConfig config;

    private final SecureRandom random = new SecureRandom();
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public LinkService(LinkRepository repo, NotificationService notifier, AppConfig config) {
        this.repo = repo;
        this.notifier = notifier;
        this.config = config;
    }

    private String normalizeShortUrl(String input) {
        String s = input.trim();
        if (s.startsWith("http://")) s = s.substring(7);
        if (s.startsWith("https://")) s = s.substring(8);
        return s;
    }

    private String generateCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String generateUniqueShortUrl() {
        while (true) {
            String code = generateCode(7);
            String shortUrl = config.baseUrl() + "/" + code;
            if (!repo.exists(shortUrl)) return shortUrl;
        }
    }

    public Link createLink(UUID ownerId, String originalUrl, Integer maxClicksOrNull) {
        validateUrl(originalUrl);
        int maxClicks = (maxClicksOrNull == null) ? config.defaultMaxClicks() : maxClicksOrNull;
        if (maxClicks < 0) throw new IllegalArgumentException("Лимит должен быть >= 0.");

        Instant now = Instant.now();
        Instant expiresAt = now.plus(config.ttlHours(), ChronoUnit.HOURS);

        String shortUrl = generateUniqueShortUrl();
        Link link = new Link(originalUrl.trim(), shortUrl, ownerId, maxClicks, now, expiresAt);
        repo.save(link);
        return link;
    }

    public void openLink(String shortUrlInput) {
        String shortUrl = normalizeShortUrl(shortUrlInput);

        Link link = repo.findByShortUrl(shortUrl).orElse(null);
        if (link == null) {
            System.out.println("Ссылка не найдена.");
            return;
        }

        Instant now = Instant.now();

        if (link.isExpired(now)) {
            notifier.push(link.getOwnerId(), "TTL истёк, ссылка удалена: " + link.getShortUrl());
            repo.delete(link.getShortUrl());
            System.out.println("Ссылка недоступна: истёк срок действия.");
            return;
        }

        if (link.isLimitReached()) {
            notifier.push(link.getOwnerId(), "Лимит переходов исчерпан: " + link.getShortUrl());
            System.out.println("Ссылка недоступна: лимит переходов исчерпан.");
            return;
        }

        link.incrementClicks();
        repo.save(link);

        if (link.isLimitReached()) {
            notifier.push(link.getOwnerId(), "Лимит переходов исчерпан: " + link.getShortUrl());
        }

        System.out.println("Открывается: " + link.getOriginalUrl());

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(link.getOriginalUrl()));
            } else {
                System.out.println("Desktop browse недоступен. Откройте вручную: " + link.getOriginalUrl());
            }
        } catch (Exception e) {
            System.out.println("Ошибка при открытии: " + e.getMessage());
        }
    }

    public void updateLimit(UUID actorId, String shortUrlInput, int newLimit) {
        String shortUrl = normalizeShortUrl(shortUrlInput);
        Link link = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена."));

        link.updateMaxClicks(actorId, newLimit);
        repo.save(link);
    }

    public void delete(UUID actorId, String shortUrlInput) {
        String shortUrl = normalizeShortUrl(shortUrlInput);
        Link link = repo.findByShortUrl(shortUrl)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена."));

        if (!link.getOwnerId().equals(actorId)) {
            throw new IllegalStateException("Нет прав: вы не владелец ссылки.");
        }
        repo.delete(shortUrl);
    }

    private void validateUrl(String url) {
        try {
            URI u = new URI(url.trim());
            String scheme = u.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("URL должен начинаться с http:// или https://");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный URL: " + e.getMessage());
        }
    }
}
